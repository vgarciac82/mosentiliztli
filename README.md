# Mosentiliztli - Wedding RSVP Platform 💍

Plataforma de invitaciones digitales y gestión de confirmaciones (RSVP) diseñada específicamente para bodas, con un enfoque Mobile-First y notificaciones automáticas por WhatsApp a través de Twilio.

## 🌟 Características Principales (Fase 1 MVP)

1. **Gestión de Invitados masiva**: Carga rápida de invitados mediante archivos CSV con asignación de "Pases".
2. **Distribución automatizada por WhatsApp**: Integración con Twilio Content Templates para enviar invitaciones personalizadas (con nombre del invitado y su URL única) directamente a su celular.
3. **Control de Acceso Seguro**: Autenticación doble mediante Tokens UUID (URL segura) o un código alfanumérico corto de 6 caracteres.
4. **Dashboard Administrativo en Tiempo Real**: Panel de control donde los novios pueden ver el estatus de las invitaciones (Pendientes, Confirmados, Declinados) y el llenado del aforo en tiempo real.
5. **Interfaz Mobile-First**: UI/UX interactiva construida con Thymeleaf y Tailwind CSS para que los invitados confirmen su asistencia desde su teléfono de manera intuitiva.

---

## 🛠️ Stack Tecnológico

- **Backend:** Java 25 + Spring Boot 4.x
- **Base de Datos:** PostgreSQL 15
- **Frontend:** Thymeleaf + Tailwind CSS + Vanilla JS
- **Integraciones:** Twilio SDK (WhatsApp API), OpenCSV
- **Infraestructura:** Docker + Docker Compose

---

## 🚀 Despliegue Local para Desarrollo

### Prerrequisitos
- Docker y Docker Compose
- JDK 25
- Maven (O usar el wrapper incluido `./mvnw`)

### Pasos
1. **Levantar la base de datos:**
   ```bash
   docker compose up -d db
   ```
2. **Ejecutar la aplicación Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Cargar invitados:** Envía un POST al endpoint de importación con tu CSV:
   ```bash
   curl -X POST -F "file=@invitados.csv" http://localhost:8080/api/v1/admin/guests/upload
   ```

---

## 🚢 Despliegue en Producción (VPS)

El proyecto incluye un `Dockerfile` multietapa optimizado para producción.

1. Clona el repositorio en tu servidor.
2. Inicia todo con Docker Compose:
   ```bash
   docker compose up -d --build
   ```
3. Configura las credenciales de Twilio en la base de datos:
   ```sql
   INSERT INTO event_config (total_capacity, twilio_account_sid, twilio_auth_token, twilio_phone_number) 
   VALUES (250, 'AC...', 'auth_token...', 'whatsapp:+52...');
   ```
4. Expón el puerto `8080` de tu contenedor al mundo, preferentemente detrás de un proxy reverso (Nginx/Traefik) y Cloudflare para obtener certificados HTTPS automáticos.

---

## 📂 Estructura del Código

- `/controller`: Controladores REST (Subida de CSV) y Controladores MVC (Dashboard y Vistas).
- `/service`: Lógica de negocio (Twilio, RSVP, Generación de Códigos).
- `/model` & `/dto`: Entidades JPA y objetos de transferencia de datos.
- `/resources/templates`: Vistas HTML (Thymeleaf).
- `/ai-specs`: Documentación del proyecto, reglas de negocio y PRD.

---
*Desarrollado con ♥ por TlamatiniSoft.*
