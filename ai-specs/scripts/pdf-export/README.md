# PDF Export

Este directorio contiene un script reutilizable para convertir archivos Markdown a PDF usando:

- `marked` para transformar Markdown a HTML
- `mermaid` para renderizar diagramas Mermaid
- `puppeteer-core` para controlar Microsoft Edge o Google Chrome y exportar el PDF

## Requisitos

- Node.js instalado
- `npm` disponible en terminal
- Microsoft Edge o Google Chrome instalado en Windows

## Instalación

Desde esta carpeta, instalar las dependencias:

```powershell
npm install
```

## Uso

Ejecutar el exportador con:

```powershell
npm run export -- --input <ruta-md> --output <ruta-pdf> --title "<titulo>"
```

### Uso desde Cursor

También se puede invocar desde Cursor con el comando:

```text
/export-markdown-pdf <input> <output> "<title>"
```

Reglas del comando:

- `<input>` es la ruta Markdown relativa al workspace.
- `<output>` se resuelve siempre dentro de `document/reportes/`.
- `<title>` es obligatorio y se envía al argumento `--title`.

Ejemplo:

```text
/export-markdown-pdf document/PRD.md sai_nomina_prd.pdf "SAI NOMINA PRD"
```

Parámetros:

- `--input`: ruta del archivo Markdown, relativa a la raíz del workspace
- `--output`: ruta del PDF de salida, relativa a la raíz del workspace
- `--title`: título del documento PDF

## Ejemplo para este proyecto

Ejecutar desde la carpeta de scripts/pdf-export/

```powershell
npm run export -- --input document/PRD.md --output document/reportes/SAI_NOMINA_PRD_Marzo_2026.pdf --title "SAI_NOMINA PRD Marzo 2026"
```

## Ejecución directa con Node

También se puede ejecutar sin `npm run`:

```powershell
node export-markdown-pdf.mjs --input document/PRD.md --output document/reportes/SAI_NOMINA_PRD_Marzo_2026.pdf --title "SAI_NOMINA PRD Marzo 2026"
```

## Notas

- Las rutas `--input` y `--output` se resuelven desde la raíz del repositorio.
- El script crea automáticamente la carpeta destino si no existe.
- Si el documento contiene bloques Mermaid, el script intenta renderizarlos antes de generar el PDF.
- Los diagramas Mermaid grandes usan una heurística híbrida: si caben en el espacio restante de la página actual con una reducción similar a la de una página dedicada, se mantienen inline; si no, se envían a una página dedicada y se escalan automáticamente dentro del contenedor disponible.
- Si no se encuentra Edge, el script intenta usar Chrome.
- El PDF se genera sin encabezado y el pie mantiene únicamente la paginación.
