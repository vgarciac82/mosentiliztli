# Role

You are an expert frontend architect with extensive experience in React projects applying best practices.

# Ticket ID

$ARGUMENTS

# Goal

Obtener un plan paso a paso para un ticket de Redmine listo para implementar.

# Process and rules

1. Adoptar el rol definido en `ai-specs/.agents/frontend-developer.md` (en este repo, `.claude/agents/frontend-developer.md` es un puntero al mismo archivo)
2. Analizar el ticket de Redmine indicado en #ticket usando el MCP. Si la mención es un archivo local, no uses el MCP
3. Proponer un plan paso a paso para la parte frontend, teniendo en cuenta todo lo indicado en el ticket y aplicando las buenas prácticas y reglas del proyecto en `/ai-specs/specs`
4. Aplicar las buenas prácticas de tu rol para que el desarrollador sea totalmente autónomo e implemente el ticket de extremo a extremo solo con tu plan
5. No escribir código aún; entregar solo el plan en el formato de salida definido abajo
6. Si te piden empezar a implementar en algún momento, asegúrate de que lo primero que hagas sea cambiar a un branch nombrado según el id del ticket (si aún no estás en él) y seguir el proceso descrito en el comando `/develop-frontend`

# Output format

Documento Markdown en la ruta `ai-specs/changes/[redmine_id]_frontend.md` con el detalle completo de implementación.
Seguir esta plantilla:

## Frontend Implementation Plan Ticket Template Structure

### 1. **Header**
- Title: `# Frontend Implementation Plan: [TICKET-ID] [Feature Name]`

### 2. **Overview**
- Descripción breve de la funcionalidad y de los principios de arquitectura frontend (component-based architecture, service layer, patrones React)

### 3. **Architecture Context**
- Componentes/servicios involucrados
- Archivos referenciados
- Consideraciones de routing (si aplica)
- Enfoque de state management

### 4. **Implementation Steps**
Pasos detallados, típicamente:

#### **Step 0: Create Feature Branch**
- **Action**: Crear y cambiar a un nuevo feature branch siguiendo el flujo de desarrollo. Comprobar si existe y, si no, crearlo
- **Branch Naming**: Seguir la convención de nombres del proyecto (`feature/[ticket-id]-frontend`; es obligatorio usar este formato; no permitir mantener el [ticket-id] genérico si existe para separar responsabilidades)
- **Implementation Steps**:
  1. Asegurarse de estar en el último `main` o `develop` (o la base branch adecuada)
  2. Traer últimos cambios: `git pull origin [base-branch]`
  3. Crear nuevo branch: `git checkout -b [branch-name]`
  4. Verificar la creación del branch: `git branch`
- **Notes**: Este debe ser el PRIMER paso antes de cualquier cambio de código. Consultar la sección "Development Workflow" en `ai-specs/specs/frontend-standards.mdc` para convenciones de nombres de branch y reglas de flujo

#### **Step N: [Action Name]**
- **File**: Ruta del archivo objetivo
- **Action**: Qué implementar
- **Function/Component Signature**: Firma de código
- **Implementation Steps**: Lista numerada
- **Dependencies**: Imports necesarios
- **Implementation Notes**: Detalles técnicos

Pasos habituales (ajustar rutas al árbol real del repo; referencia: `ai-specs/specs/frontend-standards.mdc`):
- **Step 1**: Capa API / hooks / TanStack Query bajo `frontend/src/features/<dominio>/` o `frontend/src/core/shared/api/`
- **Step 2**: Componentes y páginas bajo `frontend/src/features/<dominio>/components|pages/`
- **Step 3**: Routing en `frontend/src/core/shared/router/` o equivalente del proyecto; registrar rutas en el arranque de la app

#### **Step N+1: Update Technical Documentation**
- **Action**: Revisar y actualizar la documentación técnica según los cambios realizados
- **Implementation Steps**:
  1. **Review Changes**: Analizar todos los cambios de código realizados durante la implementación
  2. **Identify Documentation Files**: Determinar qué archivos de documentación hay que actualizar según:
     - Cambios en endpoints API → actualizar `ai-specs/specs/api-spec.yml`
     - Patrones UI/UX o de componentes → actualizar `ai-specs/specs/frontend-standards.mdc`
     - Cambios de routing → actualizar la documentación de routing
     - Nuevas dependencias o cambios de configuración → actualizar `ai-specs/specs/frontend-standards.mdc`
     - Patrones de test o cambios en Cypress → actualizar documentación de testing
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
- **Notes**: Este paso es OBLIGATORIO antes de dar por cerrada la implementación. No omitir actualizaciones de documentación

### 5. **Implementation Order**
- Lista numerada de pasos en secuencia (debe empezar con Step 0: Create Feature Branch y terminar con el paso de actualización de documentación)

### 6. **Testing Checklist**
- Checklist de verificación post-implementación
- Verificación de funcionalidad de componentes
- Verificación de manejo de errores

### 7. **Error Handling Patterns**
- Gestión del estado de error en componentes
- Mensajes de error claros para el usuario
- Manejo de errores de API en servicios

### 8. **UI/UX Considerations** (si aplica)
- Uso de componentes React
- Consideraciones de diseño responsive
- Requisitos de accesibilidad
- Estados de carga y feedback

### 9. **Dependencies**
- Librerías y herramientas externas necesarias
- Componentes React utilizados
- Paquetes de terceros (si los hay)

### 10. **Notes**
- Recordatorios y restricciones importantes
- Reglas de negocio
- Requisitos de idioma (terminología técnica en inglés)
- Consideraciones TypeScript vs JavaScript

### 11. **Next Steps After Implementation**
- Tareas posteriores a la implementación (la documentación ya se cubre en Step N+1; puede incluir integración, deployment, etc.)

### 12. **Implementation Verification**
- Checklist final de verificación:
  - Code Quality
  - Functionality
  - Testing
  - Integration
  - Documentation updates completed
