# ADR-NNN — [Título corto de la decisión]

> **Ruta de archivo:** `document/adrs/ADR-<nnn>-<slug>.md`
> **Cuándo usar:** Decisión arquitectónica no trivial (persistencia, semántica de dominio, idempotencia, integración externa nueva o cambio de contrato de API/esquema).

---

## Metadatos

| Campo | Valor |
|-------|-------|
| ID | ADR-NNN |
| Fecha | YYYY-MM-DD |
| Estado | Propuesto / Aceptado / Rechazado / Deprecado / Supersedido por ADR-XXX |
| Decisores | [Nombres o roles] |
| PRD Ref | US-XX / N/A |
| Ticket Redmine | #ID / N/A |

---

## Contexto

Describe el problema o la situación que requiere una decisión. Incluye:

- Qué restricciones técnicas, de negocio o de tiempo existen.
- Por qué es una decisión no trivial (¿qué hace que la alternativa obvia no sea suficiente?).
- Qué pasa si no se decide explícitamente ahora.

---

## Opciones consideradas

### Opción A — [Nombre]

Descripción breve de la opción.

**Ventajas:**
- …

**Desventajas:**
- …

### Opción B — [Nombre]

Descripción breve de la opción.

**Ventajas:**
- …

**Desventajas:**
- …

---

## Decisión

**Se elige la Opción [A/B/…] porque:**

Explica el razonamiento principal. Debe ser observable y comprobable; no opiniones genéricas.

---

## Consecuencias

### Positivas
- …

### Negativas / Trade-offs aceptados
- …

### Acciones de seguimiento
- [ ] Tarea concreta derivada (si aplica)
- [ ] Actualizar `document/PRD.md` sección X si cambia el contrato

---

## Referencias

- [PRD](../PRD.md)
- [Estándar de flujo SDD](../../ai-specs/specs/flujo-desarrollo-standards.md) Fase 3
