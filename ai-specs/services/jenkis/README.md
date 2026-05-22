# Jenkins CI/CD - Servidor de Automatización

## 📖 Descripción
Jenkins es un servidor de automatización de código abierto líder en la industria. Actúa como el motor principal para implementar prácticas de Integración Continua (CI) y Despliegue Continuo (CD). Permite a los equipos de desarrollo construir, probar y desplegar software de manera repetible, asegurando que los cambios en el código se integren de forma ágil y se entreguen con altos estándares de calidad.

## ⚙️ Funcionalidades Principales
* **Pipeline as Code:** Definición de flujos de trabajo de despliegue mediante archivos `Jenkinsfile`, lo que permite versionar la infraestructura junto con el código fuente del aplicativo.
* **Ecosistema de Plugins:** Extensibilidad masiva para integrarse con prácticamente cualquier herramienta del ciclo de vida del software (Git, Docker, Maven, SonarQube, etc.).
* **Gestión Segura de Secretos:** Integración para almacenar e inyectar credenciales (como contraseñas de bases de datos o tokens de registros de contenedores) sin exponerlos en texto plano, cumpliendo con principios DevSecOps.
* **Arquitectura Distribuida:** Capacidad de delegar cargas de trabajo distribuyendo tareas en múltiples nodos (agentes) para optimizar recursos de cómputo.
* **Automatización de Seguridad:** Facilita la inserción de pruebas de seguridad automatizadas (SAST, DAST, SCA) en las etapas tempranas del ciclo de desarrollo.

## Obtener la contraseña inicial:

El proceso de seguridad es exactamente el mismo. Para extraer la contraseña generada en el primer arranque, ejecuta:
```bash
docker exec jenkins-server cat /var/jenkins_home/secrets/initialAdminPassword
```

**Tip DevSecOps:** Si más adelante decides conectar Jenkins a tu registro privado de contenedores, este mismo archivo `docker-compose.yml` te facilitará inyectar las variables de entorno necesarias o montar certificados SSL adicionales en la sección de `volumes` para asegurar la comunicación.

## 🚀 Manual de Uso Básico

### 1. Acceso al Panel de Control
* Abre tu navegador web e ingresa a la IP de tu servidor o `http://localhost:8087`.
* Inicia sesión con tus credenciales de administrador.
* El panel principal (Dashboard) te mostrará el estado y la salud general de todos tus proyectos y *pipelines* activos.

### 2. Creación de tu Primer Pipeline
Para automatizar la construcción de un proyecto (por ejemplo, una aplicación Java con Spring Boot o la construcción de una imagen de contenedor):

1. Haz clic en **"Nueva Tarea"** (New Item) en el menú lateral izquierdo.
2. Ingresa un nombre para tu proyecto, selecciona **"Pipeline"** y haz clic en **OK**.
3. En la configuración, baja hasta la sección **Pipeline**. Selecciona **"Pipeline script from SCM"** si tienes tu `Jenkinsfile` en Git (práctica recomendada), o escribe directamente el script en la consola.
4. Un ejemplo básico de estructura de un `Jenkinsfile`:

   ```groovy
   pipeline {
       agent any
       stages {
           stage('Build & Unit Test') {
               steps {
                   echo 'Compilando el proyecto y ejecutando pruebas...'
                   sh 'mvn clean package'
               }
           }
           stage('Docker Build & Push') {
               steps {
                   echo 'Construyendo imagen Docker y subiendo al Registry privado...'
                   // Comandos de docker build y docker push
               }
           }
       }
   }
   ```
5. Guarda la configuración y haz clic en **"Construir ahora"** (Build Now) para disparar la ejecución manual.

### 3. Gestión de Credenciales
Para conectar Jenkins de forma segura con recursos externos (como un servidor PostgreSQL o tu entorno de producción):

1. En el panel principal, ve a **"Administrar Jenkins"** (Manage Jenkins) > **"Credenciales"** (Credentials).
2. Selecciona **"System"** > **"Global credentials (unrestricted)"** > **"Add Credentials"**.
3. Selecciona el tipo de credencial adecuado (por ejemplo, "Username with password" o "Secret text").
4. Ingresa los datos reales, asigna un "ID" descriptivo (ej. `postgres-db-creds`) y guarda.
5. Ahora puedes referenciar ese ID dentro de tu `Jenkinsfile` para que Jenkins pase la información de forma enmascarada a tus scripts.

### 4. Monitoreo y Solución de Errores
* **Console Output:** Si un *pipeline* falla (se marca en rojo), haz clic en el número de la ejecución fallida (ej. `#4`) y selecciona **"Console Output"**. Aquí verás el log detallado en tiempo real para identificar errores de sintaxis, fallos de red o problemas de permisos de sistema operativo.
* **Blue Ocean (Recomendado):** Para una mejor experiencia visual, instala el plugin "Blue Ocean". Te proporcionará una interfaz gráfica moderna e intuitiva para visualizar cada etapa (stage) de tu *pipeline* y encontrar rápidamente dónde ocurrió un cuello de botella o un error.