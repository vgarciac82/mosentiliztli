# ROLE

Actúa como asistente técnico del proyecto y ejecuta la exportación de un archivo Markdown a PDF usando el script reutilizable `scripts/pdf-export/export-markdown-pdf.mjs`.

# OBJECTIVE

Generar un PDF a partir de un archivo Markdown del repositorio, usando siempre:

- `--input` como ruta de entrada relativa al workspace
- `--output` dentro de `document/reportes/`
- `--title` proporcionado por el usuario en el comando

# INPUT

El comando espera `$ARGUMENTS` con este formato:

`<input> <output> <title>`

Donde:

- `<input>` es la ruta del archivo Markdown de entrada, relativa a la raíz del workspace
- `<output>` es el nombre del archivo PDF de salida o una subruta relativa dentro de `document/reportes/`
- `<title>` es el texto que se enviará en `--title`; si contiene espacios debe escribirse entre comillas

Ejemplos válidos:

- `document/PRD.md sai_nomina_prd.pdf "SAI NOMINA PRD"`
- `ai-specs/specs/flujo-desarrollo-standards.md arquitectura/flujo-desarrollo.pdf "Flujo de Desarrollo"`

Si falta `input`, `output` o `title`, pide una sola aclaración breve.

# PROCEDURE

1. Validar que el archivo de entrada exista.
2. Resolver la salida siempre bajo `document/reportes/`.
3. Si el nombre de salida no termina en `.pdf`, agregar la extensión `.pdf`.
4. Ejecutar desde `scripts/pdf-export/` el comando:

```powershell
node export-markdown-pdf.mjs --input <input> --output document/reportes/<output> --title "<title>"
```

5. Confirmar al usuario la ruta final generada.

# CONSTRAINTS

- No escribir el PDF fuera de `document/reportes/`.
- Mantener la narrativa en español y la terminología técnica en inglés.
- Si la ruta de salida proporcionada ya incluye `document/reportes/`, normalízala para evitar duplicarla.

# OUTPUT

Responder con una confirmación breve incluyendo:

- archivo Markdown de entrada
- ruta final del PDF generado
