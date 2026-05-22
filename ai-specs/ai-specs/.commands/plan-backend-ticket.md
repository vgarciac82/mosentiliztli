# Role

You are an expert software architect with extensive experience in Java Spring Boot projects applying Domain-Driven Design (DDD).

# Ticket ID

$ARGUMENTS

# Goal

Obtener un plan paso a paso para un ticket de Redmine listo para implementar.

# Process and rules

1. Adoptar el rol definido en `ai-specs/.agents/backend-developer.md` (en este repo, `.claude/agents/backend-developer.md` es un puntero al mismo archivo)
2. Analizar el ticket de Redmine indicado en #ticket usando el MCP. Si la mención es un archivo local, no uses el MCP
3. Proponer un plan paso a paso para la parte backend, teniendo en cuenta todo lo indicado en el ticket y aplicando las buenas prácticas y reglas del proyecto en `/ai-specs/specs`
4. Aplicar las buenas prácticas de tu rol para que el desarrollador sea totalmente autónomo e implemente el ticket de extremo a extremo solo con tu plan
5. No escribir código aún; entregar solo el plan en el formato de salida definido abajo
6. Si te piden empezar a implementar en algún momento, asegúrate de que lo primero que hagas sea cambiar a un branch nombrado según el id del ticket (si aún no estás en él) y seguir el proceso descrito en el comando `/develop-backend`

# Output format

Documento Markdown en la ruta `ai-specs/changes/[redmine_id]_backend.md` con el detalle completo de implementación.
Seguir esta plantilla:

## Backend Implementation Plan Ticket Template Structure

### 1. **Header**
- Title: `# Backend Implementation Plan: [TICKET-ID] [Feature Name]`

### 2. **Overview**
- Descripción breve de la funcionalidad y de los principios de arquitectura (DDD, clean architecture)

### 3. **Architecture Context**
- Capas involucradas (Domain, Application, Presentation)
- Componentes/archivos referenciados

### 4. **Implementation Steps**
Pasos detallados, típicamente:

#### **Step 0: Create Feature Branch**
- **Action**: Crear y cambiar a un nuevo feature branch siguiendo el flujo de desarrollo. Comprobar si existe y, si no, crearlo
- **Branch Naming**: Seguir la convención de nombres del proyecto (`feature/[ticket-id]-backend`; es obligatorio usar este formato; no permitir mantener el [ticket-id] genérico si existe para separar responsabilidades)
- **Implementation Steps**:
  1. Asegurarse de estar en el último `main` o `develop` (o la base branch adecuada)
  2. Traer últimos cambios: `git pull origin [base-branch]`
  3. Crear nuevo branch: `git checkout -b [branch-name]`
  4. Verificar la creación del branch: `git branch`
- **Notes**: Este debe ser el PRIMER paso antes de cualquier cambio de código. Consultar la sección "Development Workflow" en `ai-specs/specs/backend-standards.mdc` para convenciones de nombres de branch y reglas de flujo

#### **Step N: [Action Name]**
- **File**: Ruta del archivo objetivo
- **Action**: Qué implementar
- **Function Signature**: Firma de código
- **Implementation Steps**: Lista numerada
- **Dependencies**: Imports necesarios
- **Implementation Notes**: Detalles técnicos

Pasos habituales:
- **Step 1**: Crear función de validación
- **Step 2**: Crear método de servicio
- **Step 3**: Crear método de controller
- **Step 4**: Añadir ruta
- **Step 5**: Escribir unit tests (con subcategorías: Successful Cases, Validation Errors, Not Found, Reference Validation, Server Errors, Edge Cases)

Ejemplo de buena estructura:
**Implementation Steps**:

1. **Validate Position Exists**:
   - Usar `Position.findOne(positionId)` para obtener la posición existente
   - Si no existe, lanzar `new Error('Position not found')`
   - Guardar la posición existente para el merge

#### **Step N+1: Update Technical Documentation**
- **Action**: Revisar y actualizar la documentación técnica según los cambios realizados
- **Implementation Steps**:
  1. **Review Changes**: Analizar todos los cambios de código realizados durante la implementación
  2. **Identify Documentation Files**: Determinar qué archivos de documentación hay que actualizar según:
     - Cambios en el modelo de datos → actualizar `ai-specs/specs/data-model.md`
     - Cambios en endpoints API → actualizar `ai-specs/specs/api-spec.yml`
     - Cambios en estándares/librerías/config → actualizar los `*-standards.mdc` relevantes
     - Cambios de arquitectura → actualizar la documentación de arquitectura relevante
  3. **Update Documentation**: Para cada archivo afectado:
     - Actualizar el contenido en español (según `documentation-standards.mdc`)
     - Mantener la consistencia con la estructura de documentación existente
     - Asegurar el formato correcto
  4. **Verify Documentation**:
     - Confirmar que todos los cambios quedan reflejados con precisión
     - Comprobar que la documentación sigue la estructura establecida
  5. **Report Updates**: Documentar qué archivos se actualizaron y qué cambios se hicieron
- **References**:
  - Seguir el proceso descrito en `ai-specs/specs/documentation-standards.mdc`
  - Toda la documentación debe redactarse en español (narrativa; terminología técnica en inglés)
- **Notes**: Este paso es OBLIGATORIO antes de dar por cerrada la implementación. No omitir actualizaciones de documentación

### 5. **Implementation Order**
- Lista numerada de pasos en secuencia (debe empezar con Step 0: Create Feature Branch y terminar con el paso de actualización de documentación)

### 6. **Testing Checklist**
- Checklist de verificación post-implementación

### 7. **Error Response Format**
- Estructura JSON
- Mapeo de códigos de estado HTTP

### 8. **Partial Update Support** (si aplica)
- Comportamiento para actualizaciones parciales

### 9. **Dependencies**
- Librerías y herramientas externas necesarias

### 10. **Notes**
- Recordatorios y restricciones importantes
- Reglas de negocio
- Requisitos de idioma

### 11. **Next Steps After Implementation**
- Tareas posteriores a la implementación (la documentación ya se cubre en Step N+1; puede incluir integración, deployment, etc.)

### 12. **Implementation Verification**
- Checklist final de verificación:
  - Code Quality
  - Functionality
  - Testing
  - Integration
  - Documentation updates completed
