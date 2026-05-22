# Plan de Implementación: Backend MVP (Fase 1) - Invitación Boda

## 1. Contexto de Arquitectura
- **Stack:** Java, Spring Boot (3.x), Spring Data JPA, PostgreSQL.
- **Patrón:** Arquitectura en capas (Controller -> Service -> Repository -> Entity).
- **Objetivo:** Construir la API que soporte el micrositio (acceso por token, confirmación de RSVP con topes) y reciba los webhooks de Twilio.
- **Documento Referencia:** `ai-specs/document/PRD-Invitacion-Boda.md`

---

## 2. Pasos de Implementación

### Paso 1: Inicialización y Configuración Base
1. Crear un proyecto Spring Boot usando Maven o Gradle con las siguientes dependencias:
   - Spring Web (REST APIs)
   - Spring Data JPA (Persistencia)
   - PostgreSQL Driver
   - Spring Boot Validation (Validación de DTOs)
   - Lombok (Opcional, para reducir código boilerplate)
2. Configurar `application.yml` con:
   - Credenciales de PostgreSQL.
   - Dialecto JPA/Hibernate (`update` para desarrollo inicial, luego migraciones con Flyway/Liquibase).
   - Variables de entorno para el Webhook y Twilio (si aplica).

### Paso 2: Modelo de Datos (JPA Entities)
Crear las entidades respetando el PRD:
1. `EventConfig`: ID, límite de fecha de expiración, capacidad total.
2. `Guest`: ID (UUID), familia, pasesAsignados, celular, nombreInvitado, grupo, notas, token, codigoInvitacion, estatus (Enum: PENDIENTE, INVITACION_ENVIADA, CONFIRMADO, DECLINADO, RESPONDIO_WHATSAPP, ERROR_ENVIO).
3. `RsvpResponse`: ID, guest_id (Relación), pasesConfirmados, fechaConfirmacion.
4. `TwilioMessage` (Bandeja): ID, numeroRemitente, guest_id (nulo si no identificado), cuerpoMensaje, fechaRecepcion.

### Paso 3: Lógica de Negocio (Servicios)
Construir los servicios (`@Service`) para aplicar las reglas de negocio estrictas:
1. **GuestService:**
   - Búsqueda por `token` o `codigoInvitacion`.
   - Validar expiración (35 días) y retornar error/403 si está expirado.
2. **RsvpService:**
   - Procesar la confirmación.
   - **Regla de Oro:** Validar que `pases_confirmados <= pases_asignados`. Si es mayor, lanzar excepción (400 Bad Request).
   - **Idempotencia:** Validar que el estado actual no sea `DECLINADO`. Si es `DECLINADO`, rechazar la actualización. Actualizar estado a `CONFIRMADO` o `DECLINADO` y registrar el `RsvpResponse`.
3. **TwilioWebhookService:**
   - Recibir el payload de Twilio.
   - Buscar al `guest` por el número de teléfono.
   - Si existe: Actualizar estado a `RESPONDIO_WHATSAPP` y guardar mensaje asociado.
   - Si NO existe: Guardar en la "Bandeja de no identificados" (guest_id = null).

### Paso 4: Controladores API (REST)
Exponer las rutas necesarias para el Frontend:
- `GET /api/v1/invitation/{token}` -> Retorna los datos del invitado (Ocultando datos sensibles) o 404/403 si no existe o expiró.
- `POST /api/v1/invitation/login` -> Para acceso vía código (retorna el token o los datos).
- `POST /api/v1/rsvp` -> Recibe un DTO con `token`, `status_decision`, y `pases_confirmados`.
- `POST /api/v1/webhooks/twilio` -> Endpoint público consumido por Twilio.

### Paso 5: Manejo Global de Errores
Implementar un `@ControllerAdvice` para:
- Estandarizar la respuesta de error (ej. `{"error": "RSVP_LOCKED", "message": "Tu invitación ha sido declinada previamente."}`).
- Manejar violaciones de validación (400) y accesos denegados por expiración (403).

### Paso 6: Pruebas Unitarias e Integración (TDD)
- Escribir tests en JUnit y Mockito para `RsvpService` asegurando que:
  - No se puedan confirmar más pases de los asignados.
  - El estado DECLINADO sea inmutable.
  - TwilioWebhookService gestione correctamente números inexistentes.

---
**Comando Siguiente (SDD):**
Una vez revisado el plan, puedes ejecutar el comando `/develop-backend @MVP-Backend-SpringBoot.md` para que la IA comience a escribir el código paso a paso según este documento.
