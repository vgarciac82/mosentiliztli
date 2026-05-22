# Guía de recomendaciones para optimizar consumo de tokens (MCP redmine-cnf)

Esta guía agrupa prácticas para reducir el consumo de tokens al usar el MCP Redmine (redmine-cnf). El consumo depende del **tamaño de las respuestas** (payloads JSON) y del **número de llamadas** que realiza el agente. Para parámetros exactos y ejemplos completos, consulta la [Tool reference](./tool-reference.md).

---

## Resumen rápido

| Herramienta | Parámetros que reducen tokens | Recomendación |
|-------------|-------------------------------|---------------|
| `list_redmine_issues` | `fields`, `limit`, `offset`, `include_pagination_info` | Campos mínimos (`id`, `subject`, `status`), `limit` 25, paginar con `offset`. |
| `search_redmine_issues` | `fields`, `limit`, `offset`, `include_pagination_info`, `scope`, `open_issues` | Igual que listado; usar `scope`/`open_issues` para acotar resultados. |
| `get_redmine_issue` | `include_journals`, `include_attachments`, `include_custom_fields` | Poner a `false` lo que no se necesite (por defecto todos `true`). |
| `search_entire_redmine` | `limit`, `offset`, `resources` | `limit` bajo (ej. 25), `resources` a `["issues"]` o `["wiki_pages"]` si solo interesa uno. |
| `get_redmine_wiki_page` | `include_attachments` | `include_attachments=false` si no se usan adjuntos. |
| `list_redmine_projects` | — | Lista corta; evitar llamarla en bucle si ya se tiene `project_id`/identifier. |
| `summarize_project_status` | — | Usar solo cuando se necesite resumen de estado; para “issues recientes” preferir `list_redmine_issues`. |

---

## 1. Listados de issues (`list_redmine_issues`, `search_redmine_issues`)

### Campos mínimos

Usa el parámetro `fields` con solo los campos necesarios. La documentación indica una reducción de ~95% en tokens con campos mínimos frente a devolver todos.

**Recomendado para listados:**
- `fields=["id", "subject", "status"]` — lista básica
- Añadir `priority`, `assigned_to`, `updated_on` solo si el flujo los necesita

**Ejemplo:**
```python
list_redmine_issues(project_id="mi-proyecto", fields=["id", "subject", "status"])
search_redmine_issues("bug", fields=["id", "subject", "status"], limit=10)
```

### Paginación

- Mantén `limit` bajo: el valor por defecto es 25; no subas a 1000 salvo que sea estrictamente necesario.
- Usa `offset` para pedir páginas siguientes en lugar de aumentar `limit`.
- Usa `include_pagination_info=true` **solo** cuando el flujo necesite saber si hay más páginas o el total; si no, omítelo para reducir el tamaño de la respuesta.

### Herramienta adecuada

- **Listado por filtros:** Si no necesitas búsqueda por texto, usa `list_redmine_issues` con `project_id`, `status_id`, `assigned_to_id`, etc. Las respuestas son más compactas.
- **Búsqueda por texto:** Usa `search_redmine_issues` cuando necesites buscar contenido; ten en cuenta que puede incluir excerpts y ser más costoso en tokens para listados simples.

Referencia: [list_redmine_issues](tool-reference.md#list_redmine_issues), [search_redmine_issues](tool-reference.md#search_redmine_issues).

---

## 2. Detalle de un issue (`get_redmine_issue`)

### Includes opcionales

Los parámetros `include_journals`, `include_attachments` e `include_custom_fields` tienen valor por defecto `true`. Pon a `false` los que no necesites:

- **Solo detalle básico (asunto, estado, descripción):** los tres a `false`.
- **Solo comentarios:** `include_journals=true`, `include_attachments=false`, `include_custom_fields=false`.
- **Solo metadatos de adjuntos:** `include_attachments=true`, el resto según necesidad.

**Ejemplo:**
```python
get_redmine_issue(issue_id=123, include_journals=False, include_attachments=False, include_custom_fields=False)
```

### Evitar “list + N get”

No listes muchos issues y luego llames a `get_redmine_issue` para cada uno. Primero filtra y pagina bien en el listado; después abre detalle solo para los IDs que realmente necesites.

Referencia: [get_redmine_issue](tool-reference.md#get_redmine_issue).

---

## 3. Búsqueda global (`search_entire_redmine`)

- **Límite y paginación:** El valor por defecto de `limit` es 100. Usa un valor menor (por ejemplo 25) y `offset` para las siguientes páginas.
- **Alcance:** Restringe `resources` a `["issues"]` o `["wiki_pages"]` cuando solo te interese uno de los dos tipos de recurso.

**Ejemplo:**
```python
search_entire_redmine(query="instalación", limit=25, resources=["issues"])
```

Referencia: [search_entire_redmine](tool-reference.md#search_entire_redmine).

---

## 4. Wiki (`get_redmine_wiki_page`)

- Usa `include_attachments=false` si no vas a usar los enlaces de descarga de adjuntos; por defecto es `true` y se incluye metadata de adjuntos.

**Ejemplo:**
```python
get_redmine_wiki_page(project_id="docs", wiki_page_title="FAQ", include_attachments=False)
```

Referencia: [get_redmine_wiki_page](tool-reference.md#get_redmine_wiki_page).

---

## 5. Proyectos y resúmenes

### `list_redmine_projects`

No expone parámetro `fields`; la lista suele ser corta. Evita llamarla en bucle si el agente ya dispone del `project_id` o del identifier del proyecto.

### `summarize_project_status`

Devuelve un resumen rico en texto y estructura. Úsala solo cuando necesites un resumen de estado del proyecto; para “ver issues recientes” es más eficiente usar `list_redmine_issues` con filtros y un `limit` bajo.

Referencia: [list_redmine_projects](tool-reference.md#list_redmine_projects), [summarize_project_status](tool-reference.md#summarize_project_status).

---

## 6. Flujo recomendado

1. **Listar** con filtros adecuados, campos mínimos (`fields`) y `limit` bajo (por ejemplo 25).
2. **Obtener detalle** solo para los issues elegidos y con los `include_*` mínimos necesarios en `get_redmine_issue`.
3. **Pagination:** Pedir más resultados con `offset` en lugar de aumentar mucho `limit` en una sola llamada.
4. Reutilizar `project_id` o identifier cuando ya se conozcan; no volver a listar proyectos innecesariamente.

Estas prácticas permiten aprovechar los parámetros ya existentes del servidor sin modificar código; la optimización es por **uso** correcto de la API.
