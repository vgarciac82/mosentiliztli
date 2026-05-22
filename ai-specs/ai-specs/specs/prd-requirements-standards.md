# Estándar de Definición de Requerimientos (PRD → Artefactos)

Este documento define un estándar reproducible para **definir requerimientos** y derivarlos a artefactos consistentes (UX Flows, Historias de Usuario/Acceptance Criteria y ADRs).

Es intencionalmente **general (agnóstico)**: se aplica a cualquier producto o proyecto que use un flujo basado en especificaciones (SDD) o similar.

Requisito: el estándar debe ser compatible con (1) el template de PRD del equipo y (2) el "documento de análisis crítico" del dominio, si existe. Si no existen, se resuelve usando `Open Questions`.

## 1) Role & Objective

### Role
Actúas como **requirements engineer / AI requirements assistant**.

### Objective
Producir un conjunto consistente y accionable de requerimientos a partir de una idea/solicitud, garantizando:
- **Sin ambigüedades**: lo que no esté claro debe convertirse en preguntas explícitas o decisiones marcadas como `TBD`.
- **Trazabilidad**: cada requerimiento derivado debe poder explicarse como parte del PRD.
- **Verificabilidad**: todas las Historias de Usuario deben incluir **Acceptance Criteria** comprobables y mapeables a pruebas o contratos.
- **Alineación con el template**: estructura y secciones del PRD coherentes con el template del equipo.

## 2) Discovery Gate (obligatorio)

Antes de escribir cualquier sección del PRD, debes interrogar al usuario.

Regla: **no escribas líneas del PRD antes de preguntar al menos 2 aclaraciones críticas** (y tantas como se requieran para eliminar ambigüedades del dominio).

### Preguntas núcleo (mínimo 2)
1. **Core problem**: ¿qué dolor resuelve y qué pasa si no lo hacemos?
2. **Success metrics**: ¿cómo medimos éxito (KPIs) y contra qué baseline?
3. **Constraints**: ¿restricciones de tiempo, costo, stack, dependencias, o integraciones?
4. **Alcance**: ¿qué incluye y qué NO incluye (non-goals)?

### Preguntas de calidad (si aplica al contexto)
- ¿Hay supuestos existentes que no estén documentados?
- ¿Qué "decisiones difíciles" se deben tomar ahora vs después (para evitar retrocesos)?
- ¿Qué edge cases son críticos para el dominio (p. ej. estados, errores, idempotencia)?

### Regla de "hallucination"
Si el usuario no define algo, debes:
- o convertirlo en pregunta,
- o marcarlo como `TBD` de forma explícita y justificar qué se necesita para aterrizarlo.

## 3) Strict PRD Schema (plantilla base agnóstica)

Cuando se genere un PRD (o una versión nueva), debes seguir este esquema como **plantilla base agnóstica**, manteniendo la intención de cada sección.

### 3.1 Estructura obligatoria
Usa exactamente estas secciones (las subsecciones pueden variar según tu template, pero no omitas la intención):
1. **Executive Summary**
   - Problem Statement
   - Proposed Solution
   - Success Criteria (3-5 KPIs medibles)
2. **User Experience & Functionality**
   - User Personas
   - User Stories (con IDs)
   - Acceptance Criteria por historia
   - Non-Goals
3. **Technical Specifications**
   - Architecture Overview (incluye diagrama o descripción de flujo)
   - Data Structures / Reference Structures (si aplica)
   - Integration Points
   - Security & Privacy
   - Comportamiento ante errores e idempotencia (si aplica al dominio)
   - Specialized System Requirements (si aplica: AI, DSL, motor de reglas, etc.)
4. **Risks & Roadmap**
   - Phased Rollout
   - Technical Risks
   - Open Questions para stakeholders (lista clara)

> **Nota sobre requisitos especializados**: si el producto incluye componentes de IA, motores de reglas, DSL u otros sistemas especializados, se documentan como subsección dentro de `Technical Specifications`, no como sección de primer nivel. Aplica el mismo rigor de quality gate que cualquier otro componente técnico.

### 3.2 Reglas por sección (quality gates)
#### 3.2.1 Executive Summary
- `Success Criteria` debe ser **medible** (tiempo, cobertura funcional, validaciones, etc.). Evita "fácil/intuitivo".
- Si un KPI es `TBD`, debe existir una pregunta abierta concreta para stakeholders y una fecha/condición de definición.

#### 3.2.2 User Experience & Functionality
- Cada `User Story` (texto de la historia) y sus `Acceptance Criteria` se redactan en **inglés**, alineado a artefactos técnicos del proyecto (véase `base-standards.mdc`). El **Executive Summary** y la narrativa para negocio pueden permanecer en español si el equipo lo define.
- Formato obligatorio de la historia:
  - `As a [role], I want [action] so that [benefit].`
- Cada historia debe incluir `Acceptance Criteria` como lista verificable con al menos un caso de error o validación.
- `Non-Goals`: documenta exclusiones para **áreas donde haya riesgo identificado de scope creep** o donde stakeholders hayan preguntado "¿y también podría hacer X?". No hay un número mínimo fijo; la lista refleja riesgos reales, no cumple una cantidad.

#### 3.2.3 Technical Specifications
- `Architecture Overview` debe describir relaciones/flujo entre módulos.
- `Integration Points` debe especificar al menos:
  - qué sistema externo se integra (p. ej. servicio externo/sistema legado),
  - cómo fluyen los datos (por empleado, por lote, etc.),
  - cómo se valida el contrato (esquema/llamada de prueba/TBD).
- Seguridad/privacidad: si aún no está definido, se debe dejar `TBD` pero con checklist de qué evidencia se necesita.

## 4) Derivación end-to-end de artefactos

Una vez generado (o provisto) el PRD, se derivan los artefactos en este orden para asegurar consistencia.

### Principio SDD: refinamiento, no duplicación
- Los **IDs de User Story** se definen en el PRD (**User Stories con IDs**). Los UX flows modelan interacción y ramas; el trabajo posterior **refina o divide** esas US (AC más finos, errores, estados). Cada paso debe **añadir información comprobable**; no se reescribe el mismo contenido en varios niveles sin nuevo detalle.
- Si una US se divide, documenta la trazabilidad (p. ej. `US-12a` / `US-12b` referenciando `US-12`, o una tabla de desglose en el PRD).

### 4.0 Modo de derivación: mínimo (default) vs completo

**El modo mínimo es el default**. El modo completo se activa solo cuando el alcance o la complejidad lo justifican. Esto evita generar artefactos que no añaden información comprobable.

#### Tabla de decisión

| Condición | Artefacto adicional requerido |
|-----------|-------------------------------|
| 1-3 US sin nuevas integraciones ni estados complejos | Solo PRD + AC verificables. No se requieren artefactos separados. |
| Flujo de usuario con >2 ramas de error, estados o decisiones | UX Flow para esas historias específicas |
| Integración externa nueva o cambio de contrato (API, esquema) | ADR de integración |
| Decisión arquitectónica no trivial (persistencia, semántica de dominio, idempotencia) | ADR |
| El AC del PRD no es suficiente para mapear a pruebas sin reinterpretar el negocio | Archivo US separado con AC Given/When/Then refinado |
| >5 US o módulo de dominio nuevo con múltiples estados y reglas | Modo completo: UX Flows + US refinadas + ADRs según aplique |
| El equipo usa sprint planning y necesita unidades de delivery | Épicas (solo para gestión de delivery, no son artefacto SDD) |

**Regla de oro**: si un artefacto no añade información observable o comprobable que no esté ya en el PRD, no se genera.

#### Convención general de artefactos (rutas + naming)
Esta convención define **dónde** guardar los artefactos derivados. Debe ser compatible con la estructura de tu repo (si tu repo no tiene esas carpetas, créalas como parte de la entrega del estándar).

Default recomendado (si no se indica lo contrario):
- `document/prd/` (versiones del PRD)
- `document/ux-flows/` (UX Flows)
- `document/user-stories/` (Historias + Acceptance Criteria refinados)
- `document/adrs/` (ADRs)
- `document/epics/` (solo si el equipo usa sprint planning)
- `document/open-questions/` (solo si aplica)

Naming recomendado (archivos `.md`):
- UX Flows: `UX-<flowid>-<slug>.md`
- Historias/AC: `US-<id>-<slug>.md`
- ADRs: `ADR-<nnn>-<slug>.md`
- Épicas: `EPIC-<id>-<slug>.md`
- Open Questions: `OPEN-QUESTIONS-<slug>.md`

Override: el PRD puede declarar una variable conceptual `ARTIFACTS_ROOT` (por ejemplo `document/` o `ai-specs/`) y el estándar debe respetarla ajustando las rutas.

### 4.1 PRD → UX Flows (cuando aplica)

**Trigger**: se genera un UX Flow cuando una User Story del PRD tiene flujo de interacción con más de dos ramas (error, estado, decisión condicional) que no quedan explícitas en el AC del PRD, o cuando el AC no es suficiente para diseñar pruebas sin reinterpretar el negocio.

Regla: los UX Flows derivan directamente de las **User Stories del PRD**, no de agrupaciones de delivery. Cada UX Flow debe referenciar los IDs de US que modela.

Incluye como mínimo:
- Happy path por flujo principal
- Estados de error (validación, parámetros faltantes, reglas de dominio inválidas si aplica)
- Edge cases que el PRD marque como ambiguos o críticos

Formato recomendado:
- Mermaid `flowchart` o `sequenceDiagram`
- Señala claramente "qué usuario hace" y "qué sistema responde".

### 4.2 UX Flows → Historias de Usuario + Acceptance Criteria (cuando aplica)

**Trigger**: se genera un archivo US separado solo cuando el UX Flow reveló estados, ramas de error o edge cases adicionales que no están en el AC del PRD y que cambiarían la interpretación de la historia para quien la implementa o la prueba. Si el AC del PRD ya es comprobable sin reinterpretar el negocio, no se crea archivo separado.

Regla: la historia debe ser pequeña y "testable". Este paso **refina** las US e IDs ya definidos en el PRD (véase **Principio SDD** arriba); no sustituye al listado del PRD sin trazabilidad.

Los **Acceptance Criteria** deben ser lo bastante concretos para **mapear a verificación**: pruebas automatizadas (unit/integration/e2e según el equipo), checks manuales explícitos o **contract tests** / esquemas (OpenAPI, JSON Schema, etc.) cuando el proyecto los use.

Plantilla:
1. `## User Story: [US-ID - título]`
2. `As a [user type] I want [feature] So that [value].`
3. `### Acceptance Criteria`
   - Use Given/When/Then o "Given contexto / When acción / Then resultado".
   - Debe incluir:
     - casos de éxito
     - al menos 1 caso de error/validación
     - comportamiento ante estados (`closed`, `pendiente`, etc.) si aplica al dominio

Regla de AC:
- No uses criterios vagos ("se muestra correctamente").
- Debe existir un observable verificable (mensaje, código de error, reglas de validación, persistencia, etc.).

### 4.3 Historias → ADRs (Architecture Decision Records)
Regla: genera ADR solo cuando existe una decisión arquitectónica no trivial que afecte diseño, API, persistencia, o semántica del dominio.

Plantilla mínima:
- `Context`: problema y restricciones
- `Options`: alternativas evaluadas
- `Decision`: decisión tomada
- `Consequences`: pros/cons, impacto en el sistema y límites

Cuándo generar ADR:
- Ambigüedades de semántica de requerimiento (p. ej. política de agregación/selección de resultados o condicionales sin rama alternativa)
- Elecciones de integración/validación del contrato (esquema vs llamada de prueba/call sample)
- Reglas de idempotencia y persistencia

### 4.4 Épicas (solo para gestión de delivery, opcional)

Las Épicas **no son un artefacto SDD**: no añaden información comprobable que no esté ya en el PRD. Se generan únicamente cuando el equipo usa sprint planning y necesita agrupar User Stories en unidades de delivery para coordinación.

Si se generan, una épica contiene:
- Nombre (verbo + sustantivo)
- Motivación (1-2 frases referenciando KPIs del PRD)
- Historias objetivo (IDs de US)
- Non-goals de delivery (no del spec)

No forman parte del pipeline de derivación de specs; no se usan como fuente para UX Flows ni para AC.

## 5) Requirement Quality Gates (crítico)

El proceso de quality gate tiene **dos momentos** para detectar defectos antes de que se propaguen a los artefactos derivados.

### 5.1 Gate-PRD (antes de derivar artefactos)

Ejecutar **antes** de generar cualquier UX Flow, US refinada o ADR. Si algún ítem falla, corregir el PRD primero.

#### Checklist Gate-PRD
- [ ] ¿Las reglas de negocio tienen semántica unívoca: qué se calcula, cómo se calcula y cómo se combina/selecciona el resultado?
- [ ] ¿El "resultado esperado" tiene estructura y tipo(s) definidos siempre que sea relevante (por entidad → atributos → valor)?
- [ ] ¿Las condiciones de "no aplica" (ninguna regla coincide / no se cumple un estado / no hay dato disponible) tienen comportamiento definido (valor por defecto, null, error, omisión)?
- [ ] ¿El manejo de errores está definido a nivel de requerimiento: qué falla, cómo se reporta y si el sistema continúa o aborta?
- [ ] ¿Idempotencia está definida donde aplique (re-ejecución: reemplazo vs acumulación vs bloqueo)?
- [ ] ¿Estados/transiciones del "recurso de dominio" están definidos si el PRD incluye estados?

### 5.2 Anti-patrones (MUST NOT)
- No "supongas constraints". Si no existen, deben ser `TBD` o preguntas.
- No escribas historias sin Acceptance Criteria.
- No uses KPIs no medibles.
- No dejes semánticas críticas ambiguas si el dominio requiere exactitud.
- No generes artefactos derivados (UX Flows, US separadas) si no añaden información comprobable nueva respecto al PRD.

### 5.3 Gate-final (después de derivar artefactos)

Ejecutar al cerrar el paquete de requerimientos para validar integridad y trazabilidad.

#### Checklist Gate-final
Marcar cada ítem como:
- `✅ Resuelto`
- `❓ Pregunta abierta para stakeholders`

- [ ] ¿Los identificadores y relaciones entre artefactos (PRD ↔ UX Flows ↔ Historias) están definidos sin contradicciones?
- [ ] ¿Hay criterios de éxito y métricas medibles asociadas a historias/capacidades?
- [ ] ¿Cada AC del paquete puede mapearse a una prueba o contrato sin reinterpretar el negocio?
- [ ] ¿Los artefactos generados (UX Flows, US refinadas, ADRs) añaden información que no está en el PRD, o son duplicación? (Si son duplicación, eliminarlos.)

#### Activadores por dominio (checklist adicional según contexto)

Añadir los siguientes ítems al Gate-final cuando aplique el dominio:

**Dominio con estados y transiciones** (ej. periodos, procesos, documentos con ciclo de vida):
- [ ] ¿Todas las transiciones de estado están definidas (incluyendo reaperturas, cancelaciones, bloqueos)?
- [ ] ¿Se especificó si las transiciones requieren auditoría o motivo?

**Dominio con motor de reglas o DSL**:
- [ ] ¿La semántica de agregación de resultados múltiples está definida (suma, primero que aplica, otro)?
- [ ] ¿El comportamiento ante expresión sin resultado (ninguna condición verdadera) está especificado?
- [ ] ¿Las funciones o referencias entre reglas tienen semántica unívoca (case-sensitivity, orden de evaluación)?

**Dominio con integraciones externas**:
- [ ] ¿El flujo de obtención de datos está definido (por entidad, por lote, caché, reintentos)?
- [ ] ¿La validación del contrato de integración está especificada (esquema estático, respuesta de prueba, o ambos)?
- [ ] ¿El comportamiento ante campos ausentes o null en la respuesta del sistema externo está definido?

### 5.4 Salida final obligatoria (Open Questions)
Si al final quedan `TBD` o preguntas sin resolver, la salida final debe incluir:
- `Open Questions`: lista de preguntas concretas
- `Impact`: por qué importa cada pregunta (impacto en diseño/implementación y riesgo)
- `Owner sugerido`: stakeholder o rol (si el usuario lo sabe)

## 6) Meta-prompt (plantilla utilizable — proceso iterativo)

Este bloque está pensado para que lo uses como guía de fases al generar PRDs y derivados. **Ejecuta una fase a la vez**; no generes todos los artefactos en una sola pasada.

```text
ROLE:
Eres un experto en requerimientos (requirements engineer) y arquitectura basada en especificaciones.

PROCESO (ejecutar fase a fase, no todo en una pasada):

── FASE 1: DISCOVERY ──────────────────────────────────────────
Antes de escribir cualquier sección del PRD:
1. Haz al menos 2 preguntas críticas de aclaración (core problem, success metrics, constraints, alcance).
2. No asumas constraints o semánticas. Si falta info, usa preguntas o marca TBD.
3. Espera las respuestas antes de continuar.

── FASE 2: PRD ────────────────────────────────────────────────
Genera el PRD con la siguiente estructura:
1) Executive Summary
   - Problem Statement
   - Proposed Solution
   - Success Criteria (3-5 KPIs medibles; TBD con fecha/condición si no está definido)
2) User Experience & Functionality
   - User Personas
   - User Stories (con IDs)
   - Acceptance Criteria por cada US (en inglés; incluye al menos 1 caso de error)
   - Non-Goals (áreas con riesgo de scope creep identificado)
3) Technical Specifications
   - Architecture Overview (con diagrama o descripción de flujo)
   - Data Structures / Reference Structures (si aplica)
   - Integration Points
   - Security & Privacy
   - Error handling + idempotencia (si aplica)
   - Specialized System Requirements (si aplica: AI, DSL, motor de reglas, etc.)
4) Risks & Roadmap

Luego ejecuta el Gate-PRD (checklist §5.1) antes de continuar.
Si algún ítem falla, corrige el PRD primero.

── FASE 3: DECISIÓN DE MODO ───────────────────────────────────
Aplica la tabla de decisión (§4.0) para determinar qué artefactos derivar:
- ¿Alguna US tiene >2 ramas de error o estados no explícitos en el AC? → UX Flow
- ¿El AC del PRD no es suficiente para mapear a pruebas sin reinterpretar? → US archivo separado
- ¿Hay decisiones arquitectónicas no triviales (integración, persistencia, semántica)? → ADR
- Si ninguna condición aplica: el PRD + AC es suficiente (modo mínimo).
Documenta la decisión tomada.

── FASE 4: DERIVACIÓN (solo artefactos justificados) ──────────
Genera únicamente los artefactos que la Fase 3 justificó:
- UX Flows (si aplica): happy path + errores + edge cases; referencia IDs de US
- US refinadas (si aplica): Given/When/Then; solo el delta respecto al AC del PRD
- ADRs (si aplica): Context / Options / Decision / Consequences

── FASE 5: GATE-FINAL + OPEN QUESTIONS ───────────────────────
1) Ejecuta Gate-final (checklist §5.3) marcando ✅ / ❓ / N/A con justificación.
   Incluye activadores de dominio que apliquen.
2) Si quedan TBD o ❓: genera sección Open Questions con impacto y owner sugerido.

INPUTS:
- Contexto del producto/feature:
  [pega aquí la idea o información disponible]
- Restricciones conocidas (si existen):
  [stack, fechas, APIs, etc.]
- (Opcional) Convención de artefactos:
  ARTIFACTS_ROOT: [ruta base], si tu repo difiere del default
```

## 7) Criterios de éxito del estándar

Se considera que el estándar está bien aplicado cuando:
- Produce PRDs consistentes y alineados al template del equipo.
- Convierte ambigüedades de requerimiento en preguntas o decisiones explícitas.
- Obliga discovery y evita "hallucinar constraints".
- Cada historia termina con Acceptance Criteria verificables y con casos edge/error, y esos criterios pueden **trazarse** a pruebas o contratos sin reinterpretar el negocio.
- La derivación **añade precisión** en lugar de duplicar el PRD; se usa **modo mínimo** por defecto y modo completo solo cuando la tabla de decisión lo justifica.
- Si hay decisiones de arquitectura, se documentan en ADRs.
- El Gate-PRD se ejecuta antes de derivar artefactos; el Gate-final cierra el paquete con trazabilidad verificada.
