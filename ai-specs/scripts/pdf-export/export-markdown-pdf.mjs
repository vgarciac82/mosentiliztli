import { existsSync } from "node:fs";
import { mkdir, mkdtemp, readFile, rm, stat, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import os from "node:os";
import { marked } from "marked";
import puppeteer from "puppeteer-core";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const workspaceRoot = path.resolve(__dirname, "..", "..");
const CSS_DPI = 96;
const PDF_PAGE_SIZE_MM = Object.freeze({
  width: 215.9,
  height: 279.4,
});
const PDF_MARGIN_MM = Object.freeze({
  top: 18,
  right: 14,
  bottom: 18,
  left: 14,
});
const MERMAID_CONTAINER_BORDER_PX = 1;
const MERMAID_CONTAINER_PADDING_PX = 16;
const MERMAID_PAGE_FIT_PADDING_PX = 8;
const INLINE_LAYOUT_SCALE_TOLERANCE = 0.9;
const PDF_PRINTABLE_SIZE_PX = getPrintablePageSizePx(PDF_PAGE_SIZE_MM, PDF_MARGIN_MM);
const PDF_PAGE_FIT_FRAME_SIZE_PX = getInnerBoxSizePx({
  width: PDF_PRINTABLE_SIZE_PX.width,
  height: PDF_PRINTABLE_SIZE_PX.height,
  padding: MERMAID_PAGE_FIT_PADDING_PX,
  borderWidth: MERMAID_CONTAINER_BORDER_PX,
});

function parseArgs(argv) {
  const options = {};

  for (let index = 0; index < argv.length; index += 1) {
    const token = argv[index];

    if (!token.startsWith("--")) {
      continue;
    }

    const [rawKey, rawValue] = token.split("=", 2);
    const key = rawKey.slice(2);

    if (rawValue !== undefined) {
      options[key] = rawValue;
      continue;
    }

    const nextToken = argv[index + 1];
    if (!nextToken || nextToken.startsWith("--")) {
      options[key] = true;
      continue;
    }

    options[key] = nextToken;
    index += 1;
  }

  return options;
}

function requireOption(options, key) {
  const value = options[key];

  if (!value || typeof value !== "string") {
    throw new Error(`Missing required option --${key}`);
  }

  return path.resolve(workspaceRoot, value);
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function decodeHtml(value) {
  return value
    .replaceAll("&quot;", '"')
    .replaceAll("&#39;", "'")
    .replaceAll("&gt;", ">")
    .replaceAll("&lt;", "<")
    .replaceAll("&amp;", "&");
}

function convertMermaidBlocks(html) {
  return html.replace(
    /<pre><code class="language-mermaid">([\s\S]*?)<\/code><\/pre>/g,
    (_match, encodedDiagram) =>
      `<div class="mermaid">${decodeHtml(encodedDiagram).trim()}</div>`,
  );
}

function mmToPx(valueInMillimeters) {
  return (valueInMillimeters * CSS_DPI) / 25.4;
}

function getPrintablePageSizePx(pageSizeMm, marginMm) {
  return {
    width: mmToPx(pageSizeMm.width - marginMm.left - marginMm.right),
    height: mmToPx(pageSizeMm.height - marginMm.top - marginMm.bottom),
  };
}

function getInnerBoxSizePx({ width, height, padding, borderWidth }) {
  const horizontalInset = (padding + borderWidth) * 2;
  const verticalInset = (padding + borderWidth) * 2;

  return {
    width: Math.max(width - horizontalInset, 0),
    height: Math.max(height - verticalInset, 0),
  };
}

function calculateDiagramScale({ diagramWidth, diagramHeight, maxWidth, maxHeight }) {
  if (
    diagramWidth <= 0
    || diagramHeight <= 0
    || maxWidth <= 0
    || maxHeight <= 0
  ) {
    return 1;
  }

  return Math.min(maxWidth / diagramWidth, maxHeight / diagramHeight, 1);
}

function getRemainingPageHeight({ offsetTop, printableHeight }) {
  if (printableHeight <= 0) {
    return 0;
  }

  const pageOffset = offsetTop % printableHeight;
  if (Math.abs(pageOffset) < 0.01) {
    return printableHeight;
  }

  return printableHeight - pageOffset;
}

function canFitDiagramInBox({ diagramWidth, diagramHeight, maxWidth, maxHeight }) {
  if (
    diagramWidth <= 0
    || diagramHeight <= 0
    || maxWidth <= 0
    || maxHeight <= 0
  ) {
    return false;
  }

  const scale = calculateDiagramScale({
    diagramWidth,
    diagramHeight,
    maxWidth,
    maxHeight,
  });

  return (diagramWidth * scale) <= (maxWidth + 0.01)
    && (diagramHeight * scale) <= (maxHeight + 0.01);
}

function selectDiagramLayout({
  diagramWidth,
  diagramHeight,
  currentPageBox,
  dedicatedPageBox,
  inlineScaleTolerance = INLINE_LAYOUT_SCALE_TOLERANCE,
}) {
  const inlineScale = calculateDiagramScale({
    diagramWidth,
    diagramHeight,
    maxWidth: currentPageBox.width,
    maxHeight: currentPageBox.height,
  });
  const dedicatedScale = calculateDiagramScale({
    diagramWidth,
    diagramHeight,
    maxWidth: dedicatedPageBox.width,
    maxHeight: dedicatedPageBox.height,
  });
  const canFitInline = canFitDiagramInBox({
    diagramWidth,
    diagramHeight,
    maxWidth: currentPageBox.width,
    maxHeight: currentPageBox.height,
  });

  if (canFitInline && inlineScale >= (dedicatedScale * inlineScaleTolerance)) {
    return {
      mode: "inline",
      scale: inlineScale,
    };
  }

  return {
    mode: "page-fit",
    scale: dedicatedScale,
  };
}

function buildHtmlDocument({ bodyHtml, title, mermaidModuleUrl }) {
  return `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${escapeHtml(title)}</title>
  <style>
    @page {
      size: letter;
      margin: 18mm 14mm 18mm 14mm;
    }

    :root {
      color-scheme: light;
      --text: #17202a;
      --muted: #5d6d7e;
      --rule: #d5dbdb;
      --surface: #f8f9f9;
      --surface-strong: #ecf0f1;
      --accent: #0b6e4f;
      --link: #0a66c2;
      --code: #7d3c98;
      --page-content-width: ${PDF_PRINTABLE_SIZE_PX.width.toFixed(2)}px;
      --page-content-height: ${PDF_PRINTABLE_SIZE_PX.height.toFixed(2)}px;
      --mermaid-padding: ${MERMAID_CONTAINER_PADDING_PX}px;
      --mermaid-page-fit-padding: ${MERMAID_PAGE_FIT_PADDING_PX}px;
      --mermaid-border-width: ${MERMAID_CONTAINER_BORDER_PX}px;
    }

    * {
      box-sizing: border-box;
    }

    html {
      font-size: 11pt;
    }

    body {
      margin: 0;
      color: var(--text);
      font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
      line-height: 1.55;
      background: #ffffff;
    }

    main {
      width: 100%;
      margin: 0 auto;
    }

    h1, h2, h3, h4, h5, h6 {
      color: #0f1720;
      line-height: 1.2;
      margin: 1.4rem 0 0.7rem;
      page-break-after: avoid;
    }

    h1 {
      font-size: 1.9rem;
      border-bottom: 2px solid var(--accent);
      padding-bottom: 0.45rem;
      margin-top: 0;
    }

    h2 {
      font-size: 1.35rem;
      border-bottom: 1px solid var(--rule);
      padding-bottom: 0.25rem;
    }

    h3 {
      font-size: 1.1rem;
    }

    p, ul, ol, table, pre, blockquote {
      margin: 0 0 0.9rem;
    }

    ul, ol {
      padding-left: 1.5rem;
    }

    li + li {
      margin-top: 0.25rem;
    }

    a {
      color: var(--link);
      text-decoration: none;
      word-break: break-word;
    }

    hr {
      border: 0;
      border-top: 1px solid var(--rule);
      margin: 1.4rem 0;
    }

    blockquote {
      margin-left: 0;
      padding: 0.75rem 1rem;
      color: var(--muted);
      border-left: 4px solid var(--accent);
      background: var(--surface);
    }

    code {
      font-family: Consolas, "Courier New", monospace;
      color: var(--code);
      background: #f4ecf7;
      padding: 0.08rem 0.28rem;
      border-radius: 4px;
      font-size: 0.92em;
    }

    pre {
      overflow: hidden;
      white-space: pre-wrap;
      word-break: break-word;
      background: #111827;
      color: #f9fafb;
      padding: 0.95rem 1rem;
      border-radius: 10px;
      page-break-inside: avoid;
    }

    pre code {
      background: transparent;
      color: inherit;
      padding: 0;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      page-break-inside: avoid;
      font-size: 0.95rem;
    }

    thead {
      display: table-header-group;
    }

    th, td {
      border: 1px solid var(--rule);
      padding: 0.55rem 0.65rem;
      vertical-align: top;
      text-align: left;
    }

    th {
      background: var(--surface-strong);
      color: #17202a;
    }

    tr:nth-child(even) td {
      background: #fbfcfc;
    }

    img, svg {
      max-width: 100%;
      height: auto;
      page-break-inside: avoid;
    }

    .mermaid {
      margin: 1rem 0 1.25rem;
      padding: var(--mermaid-padding);
      border: var(--mermaid-border-width) solid var(--rule);
      border-radius: 12px;
      background: #ffffff;
      width: min(100%, var(--page-content-width));
      page-break-inside: avoid;
      break-inside: avoid-page;
      overflow: hidden;
    }

    .mermaid.page-fit {
      margin: 0;
      width: var(--page-content-width);
      height: var(--page-content-height);
      max-width: var(--page-content-width);
      max-height: var(--page-content-height);
      padding: var(--mermaid-page-fit-padding);
      page-break-before: always;
      break-before: page;
      page-break-after: always;
      break-after: page;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .mermaid__frame {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
    }

    .mermaid.page-fit .mermaid__frame {
      flex: 1 1 auto;
      width: 100%;
      height: 100%;
      min-width: 0;
      min-height: 0;
    }

    .mermaid svg {
      display: block;
      margin: 0 auto;
      max-width: 100%;
      height: auto;
    }

    .page-break {
      page-break-before: always;
    }
  </style>
</head>
<body>
  <main class="markdown-body">
    ${bodyHtml}
  </main>
  <script type="module">
    import mermaid from "${mermaidModuleUrl}";

    mermaid.initialize({
      startOnLoad: false,
      securityLevel: "loose",
      theme: "default",
      fontFamily: "Segoe UI, Tahoma, Geneva, Verdana, sans-serif"
    });

    const printableWidth = ${PDF_PRINTABLE_SIZE_PX.width.toFixed(2)};
    const printableHeight = ${PDF_PRINTABLE_SIZE_PX.height.toFixed(2)};
    const pageFitFrameWidth = ${PDF_PAGE_FIT_FRAME_SIZE_PX.width.toFixed(2)};
    const pageFitFrameHeight = ${PDF_PAGE_FIT_FRAME_SIZE_PX.height.toFixed(2)};
    const inlineScaleTolerance = ${INLINE_LAYOUT_SCALE_TOLERANCE.toFixed(2)};
    const calculateScale = ({ diagramWidth, diagramHeight, maxWidth, maxHeight }) => {
      if (
        diagramWidth <= 0
        || diagramHeight <= 0
        || maxWidth <= 0
        || maxHeight <= 0
      ) {
        return 1;
      }

      return Math.min(maxWidth / diagramWidth, maxHeight / diagramHeight, 1);
    };

    const getRemainingHeight = ({ offsetTop, pageHeight }) => {
      if (pageHeight <= 0) {
        return 0;
      }

      const pageOffset = offsetTop % pageHeight;
      if (Math.abs(pageOffset) < 0.01) {
        return pageHeight;
      }

      return pageHeight - pageOffset;
    };

    const canFitInBox = ({ diagramWidth, diagramHeight, maxWidth, maxHeight }) => {
      if (
        diagramWidth <= 0
        || diagramHeight <= 0
        || maxWidth <= 0
        || maxHeight <= 0
      ) {
        return false;
      }

      const scale = calculateScale({
        diagramWidth,
        diagramHeight,
        maxWidth,
        maxHeight,
      });

      return (diagramWidth * scale) <= (maxWidth + 0.01)
        && (diagramHeight * scale) <= (maxHeight + 0.01);
    };

    const chooseLayout = ({ diagramWidth, diagramHeight, currentPageBox, dedicatedPageBox }) => {
      const inlineScale = calculateScale({
        diagramWidth,
        diagramHeight,
        maxWidth: currentPageBox.width,
        maxHeight: currentPageBox.height,
      });
      const dedicatedScale = calculateScale({
        diagramWidth,
        diagramHeight,
        maxWidth: dedicatedPageBox.width,
        maxHeight: dedicatedPageBox.height,
      });
      const canFitInline = canFitInBox({
        diagramWidth,
        diagramHeight,
        maxWidth: currentPageBox.width,
        maxHeight: currentPageBox.height,
      });

      if (canFitInline && inlineScale >= (dedicatedScale * inlineScaleTolerance)) {
        return { mode: "inline", scale: inlineScale };
      }

      return { mode: "page-fit", scale: dedicatedScale };
    };

    const getSvgDimension = (svg, axis) => {
      const viewBox = svg.viewBox?.baseVal;
      if (viewBox && viewBox[axis] > 0) {
        return viewBox[axis];
      }

      const attributeValue = Number.parseFloat(svg.getAttribute(axis === "width" ? "width" : "height") ?? "");
      if (Number.isFinite(attributeValue) && attributeValue > 0) {
        return attributeValue;
      }

      const bounds = svg.getBBox();
      return axis === "width" ? bounds.width : bounds.height;
    };

    const fitMermaidDiagrams = () => {
      const diagrams = document.querySelectorAll(".mermaid");

      diagrams.forEach((diagram) => {
        const svg = diagram.querySelector("svg");
        if (!svg) {
          return;
        }

        let frame = diagram.querySelector(".mermaid__frame");
        if (!frame) {
          frame = document.createElement("div");
          frame.className = "mermaid__frame";

          while (diagram.firstChild) {
            frame.appendChild(diagram.firstChild);
          }

          diagram.appendChild(frame);
        }

        const diagramWidth = getSvgDimension(svg, "width");
        const diagramHeight = getSvgDimension(svg, "height");
        if (!(diagramWidth > 0) || !(diagramHeight > 0)) {
          return;
        }

        const measuredTop = diagram.getBoundingClientRect().top + window.scrollY;
        const remainingPageHeight = getRemainingHeight({
          offsetTop: measuredTop,
          pageHeight: printableHeight,
        });
        const measuredWidth = Math.min(
          frame.getBoundingClientRect().width || printableWidth,
          printableWidth,
        );
        const currentPageFrame = {
          width: Math.max(
            measuredWidth - ((${MERMAID_CONTAINER_PADDING_PX} + ${MERMAID_CONTAINER_BORDER_PX}) * 2),
            0,
          ),
          height: Math.max(
            remainingPageHeight - ((${MERMAID_CONTAINER_PADDING_PX} + ${MERMAID_CONTAINER_BORDER_PX}) * 2),
            0,
          ),
        };
        const dedicatedPageFrame = {
          width: pageFitFrameWidth,
          height: pageFitFrameHeight,
        };
        const layout = chooseLayout({
          diagramWidth,
          diagramHeight,
          currentPageBox: currentPageFrame,
          dedicatedPageBox: dedicatedPageFrame,
        });
        const requiresPageFit = layout.mode === "page-fit";
        diagram.classList.toggle("page-fit", requiresPageFit);

        svg.style.maxWidth = "none";
        svg.style.width = \`\${diagramWidth * layout.scale}px\`;
        svg.style.height = \`\${diagramHeight * layout.scale}px\`;
        svg.style.display = "block";
        svg.style.margin = "0 auto";
        svg.setAttribute("preserveAspectRatio", "xMidYMid meet");
      });
    };

    const run = async () => {
      try {
        const diagrams = document.querySelectorAll(".mermaid");
        if (diagrams.length > 0) {
          await mermaid.run({ nodes: diagrams });
          fitMermaidDiagrams();
        }
      } finally {
        document.body.setAttribute("data-render-complete", "true");
      }
    };

    run();
  </script>
</body>
</html>`;
}

function resolveBrowserExecutable() {
  const candidates = [
    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
  ];

  return candidates.find((candidate) => existsSync(candidate));
}

async function renderPdfWithBrowser({ browserExecutable, htmlPath, outputPath }) {
  const browser = await puppeteer.launch({
    executablePath: browserExecutable,
    headless: true,
    args: [
      "--allow-file-access-from-files",
      "--disable-gpu",
      "--no-default-browser-check",
      "--no-first-run",
    ],
  });

  try {
    const page = await browser.newPage();
    await page.goto(pathToFileURL(htmlPath).href, {
      waitUntil: "networkidle0",
    });

    await page.waitForSelector("body[data-render-complete='true']", {
      timeout: 15000,
    });

    await page.pdf({
      path: outputPath,
      format: "Letter",
      printBackground: true,
      preferCSSPageSize: true,
      displayHeaderFooter: true,
      headerTemplate: "<div></div>",
      footerTemplate: `
        <div style="width:100%; font-size:8px; color:#5d6d7e; text-align:center; padding:0 14mm;">
          <span class="pageNumber"></span> / <span class="totalPages"></span>
        </div>
      `,
      margin: {
        top: "18mm",
        right: "14mm",
        bottom: "18mm",
        left: "14mm",
      },
    });
  } finally {
    await browser.close();
  }
}

async function main() {
  const options = parseArgs(process.argv.slice(2));
  const inputPath = requireOption(options, "input");
  const outputPath = requireOption(options, "output");
  const title = typeof options.title === "string"
    ? options.title
    : path.basename(outputPath, path.extname(outputPath));

  const browserExecutable = resolveBrowserExecutable();
  if (!browserExecutable) {
    throw new Error("No compatible Chromium browser was found.");
  }

  const inputMarkdown = await readFile(inputPath, "utf8");

  marked.setOptions({
    gfm: true,
    breaks: false,
  });

  const renderedHtml = convertMermaidBlocks(await marked.parse(inputMarkdown));
  const tempDirectory = await mkdtemp(path.join(os.tmpdir(), "markdown-pdf-"));
  const tempHtmlPath = path.join(tempDirectory, "document.html");
  const mermaidModulePath = path.resolve(
    __dirname,
    "node_modules",
    "mermaid",
    "dist",
    "mermaid.esm.min.mjs",
  );
  const htmlDocument = buildHtmlDocument({
    bodyHtml: renderedHtml,
    title,
    mermaidModuleUrl: pathToFileURL(mermaidModulePath).href,
  });

  await mkdir(path.dirname(outputPath), { recursive: true });
  await writeFile(tempHtmlPath, htmlDocument, "utf8");

  try {
    await renderPdfWithBrowser({
      browserExecutable,
      htmlPath: tempHtmlPath,
      outputPath,
    });
  } finally {
    await rm(tempDirectory, { recursive: true, force: true });
  }

  await stat(outputPath);
  process.stdout.write(`${outputPath}\n`);
}

if (process.argv[1] && path.resolve(process.argv[1]) === __filename) {
  main().catch((error) => {
    process.stderr.write(`${error instanceof Error ? error.stack : String(error)}\n`);
    process.exitCode = 1;
  });
}

export {
  calculateDiagramScale,
  canFitDiagramInBox,
  getInnerBoxSizePx,
  getPrintablePageSizePx,
  getRemainingPageHeight,
  mmToPx,
  selectDiagramLayout,
};
