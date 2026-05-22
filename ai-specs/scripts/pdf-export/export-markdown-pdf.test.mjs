import test from "node:test";
import assert from "node:assert/strict";
import {
  calculateDiagramScale,
  canFitDiagramInBox,
  getInnerBoxSizePx,
  getPrintablePageSizePx,
  getRemainingPageHeight,
  mmToPx,
  selectDiagramLayout,
} from "./export-markdown-pdf.mjs";

function assertClose(actual, expected, delta = 0.01) {
  assert.ok(
    Math.abs(actual - expected) <= delta,
    `Expected ${actual} to be within ${delta} of ${expected}`,
  );
}

test("mmToPx converts millimeters to CSS pixels", () => {
  assertClose(mmToPx(25.4), 96);
});

test("getPrintablePageSizePx calculates the printable area for letter size", () => {
  const printableArea = getPrintablePageSizePx(
    { width: 215.9, height: 279.4 },
    { top: 18, right: 14, bottom: 18, left: 14 },
  );

  assertClose(printableArea.width, 710.17, 0.02);
  assertClose(printableArea.height, 919.94, 0.02);
});

test("getInnerBoxSizePx subtracts padding and border from the available container space", () => {
  const innerBox = getInnerBoxSizePx({
    width: 710.17,
    height: 919.94,
    padding: 8,
    borderWidth: 1,
  });

  assertClose(innerBox.width, 692.17, 0.02);
  assertClose(innerBox.height, 901.94, 0.02);
});

test("getRemainingPageHeight returns the full page height at a page boundary", () => {
  const remainingHeight = getRemainingPageHeight({
    offsetTop: 919.94,
    printableHeight: 919.94,
  });

  assertClose(remainingHeight, 919.94, 0.02);
});

test("getRemainingPageHeight returns the remaining space on the current page", () => {
  const remainingHeight = getRemainingPageHeight({
    offsetTop: 150,
    printableHeight: 919.94,
  });

  assertClose(remainingHeight, 769.94, 0.02);
});

test("calculateDiagramScale keeps diagrams that already fit at full size", () => {
  const scale = calculateDiagramScale({
    diagramWidth: 640,
    diagramHeight: 480,
    maxWidth: 710.17,
    maxHeight: 919.94,
  });

  assert.equal(scale, 1);
});

test("calculateDiagramScale reduces diagrams that are too wide", () => {
  const scale = calculateDiagramScale({
    diagramWidth: 1200,
    diagramHeight: 600,
    maxWidth: 710.17,
    maxHeight: 919.94,
  });

  assertClose(scale, 0.5918, 0.0002);
});

test("calculateDiagramScale reduces diagrams that are too tall", () => {
  const scale = calculateDiagramScale({
    diagramWidth: 600,
    diagramHeight: 1400,
    maxWidth: 710.17,
    maxHeight: 919.94,
  });

  assertClose(scale, 0.6571, 0.0002);
});

test("calculateDiagramScale uses the tighter constraint when both dimensions overflow", () => {
  const scale = calculateDiagramScale({
    diagramWidth: 1600,
    diagramHeight: 2000,
    maxWidth: 710.17,
    maxHeight: 919.94,
  });

  assertClose(scale, 0.444, 0.001);
});

test("canFitDiagramInBox returns false when the available height is exhausted", () => {
  const canFit = canFitDiagramInBox({
    diagramWidth: 900,
    diagramHeight: 500,
    maxWidth: 676.17,
    maxHeight: 0,
  });

  assert.equal(canFit, false);
});

test("selectDiagramLayout keeps the diagram inline when remaining space yields a similar scale", () => {
  const layout = selectDiagramLayout({
    diagramWidth: 1100,
    diagramHeight: 600,
    currentPageBox: {
      width: 676.17,
      height: 820,
    },
    dedicatedPageBox: {
      width: 692.17,
      height: 901.94,
    },
  });

  assert.equal(layout.mode, "inline");
  assert.ok(layout.scale > 0.61 && layout.scale < 0.62);
});

test("selectDiagramLayout uses a dedicated page when inline space would shrink the diagram too much", () => {
  const layout = selectDiagramLayout({
    diagramWidth: 900,
    diagramHeight: 500,
    currentPageBox: {
      width: 676.17,
      height: 300,
    },
    dedicatedPageBox: {
      width: 692.17,
      height: 901.94,
    },
  });

  assert.equal(layout.mode, "page-fit");
  assert.ok(layout.scale > 0.76 && layout.scale < 0.77);
});
