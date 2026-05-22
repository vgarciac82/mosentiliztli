# Meta-prompt: Base de diseño para ERP gubernamental

Use este prompt en una herramienta de IA para generar o refinar la base de diseño y estilo gráfico aplicable a múltiples módulos del ERP gubernamental.

---

## Role

Eres un experto en design systems y UX para software institucional y gubernamental. Conoces estándares de accesibilidad (WCAG), patrones de interfaz para aplicaciones empresariales y buenas prácticas de diseño consistente en sistemas multi-módulo.

---

## Objective

Definir una **base estable de diseño y estilo gráfico** (design base) aplicable a múltiples módulos de un ERP gubernamental. No existe manual de marca previo; la base debe proponerse desde cero con criterios adecuados al contexto gubernamental (seriedad, accesibilidad, claridad, consistencia entre módulos).

---

## Context

- **Producto**: ERP gubernamental con múltiples módulos (ej. nómina, documentación, organigrama, etc.).
- **Stack**: React, Tailwind CSS (utility-first).
- **Requisitos**: consistencia visual entre módulos, accesibilidad (WCAG 2.1 AA como mínimo), tono institucional, mantenibilidad a largo plazo y reutilización de tokens vía Tailwind.

---

## Scope

**Incluir:**

- Principios de diseño (claridad, consistencia, accesibilidad, tono institucional, neutralidad).
- Paleta de color: primaria, secundaria, semántica (éxito, advertencia, error, información), neutros (fondos, bordes, texto).
- Tipografía: familia(s), escala (tamaños y line-height), uso por tipo de contenido (títulos, cuerpo, etiquetas, código).
- Escala de espaciado y grid (columnas, gutter, breakpoints).
- Criterios para componentes base: botones (primario, secundario, terciario, peligro; estados), campos de formulario, tablas, navegación (menú, breadcrumbs, tabs).
- Criterios de accesibilidad y contraste (WCAG 2.1 AA; no usar color como única información).
- Cómo se materializa en Tailwind: dónde viven los tokens (theme/tailwind.config), convención de nombres (ej. `primary-500`, `text-body`, `space-block`).

**Excluir:**

- Diseño de pantallas concretas o flujos por módulo.
- Implementación en código de componentes (solo reglas y tokens).

---

## Format

Responde de forma **estructurada por secciones**:

1. **Principios de diseño** – enunciados breves.
2. **Colores** – paleta con nombres y valores concretos (hex o equivalentes); criterio de contraste.
3. **Tipografía** – familia(s), escala (tamaño/line-height), uso recomendado.
4. **Espaciado y layout** – escala de espaciado, grid, breakpoints, convenciones de márgenes.
5. **Componentes base** – reglas y tokens por tipo (botones, formularios, tablas, navegación); estados (default, hover, focus, disabled, error).
6. **Accesibilidad** – contraste, focus visible, tamaños mínimos de touch/click.
7. **Integración Tailwind** – ubicación de tokens (archivo/config), convención de nombres y ejemplos de clases o variables.

Incluye **valores concretos** y **ejemplos de clases o variables** cuando aplique, para que puedan trasladarse directamente a `tailwind.config.js` o tema CSS.

---

## Constraints

- Todos los valores deben ser **accesibles** (contraste mínimo WCAG 2.1 AA).
- Deben ser **reutilizables** vía Tailwind en todos los módulos.
- Deben estar **alineados con estándares gubernamentales**: claridad, neutralidad, profesionalidad, prioridad a legibilidad y tareas administrativas.
- No usar color como única forma de transmitir información (complementar con iconos, texto o patrones).
