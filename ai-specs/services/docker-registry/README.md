# Docker Private Registry — Producción (DevSecOps)

Registry Docker privado autoalojado con TLS mutuo, autenticación bcrypt, escaneo de vulnerabilidades, monitoreo y backups automatizados. Diseñado para alojar imágenes internas en entornos sin dependencia de registries públicos.

## Descripción

Este servicio provee un registro de contenedores privado y soberano para equipos de desarrollo que requieren almacenar y distribuir imágenes Docker internamente, sin depender de Docker Hub u otros registries en la nube. Está orientado a entornos de producción con requisitos de seguridad y trazabilidad.

El stack combina **Docker Registry v2** como motor de almacenamiento de imágenes, **nginx** como proxy inverso con terminación TLS y rate limiting, y **MinIO** como backend de almacenamiento S3-compatible. Se complementa con **Prometheus** para métricas operativas y **Trivy** para el escaneo de vulnerabilidades bajo demanda.

## Funcionalidades

- **Almacenamiento privado de imágenes** — push y pull de imágenes con autenticación bcrypt, sin exposición pública.
- **TLS forzado** — comunicación cifrada con TLS 1.2/1.3; soporte para certificados self-signed y Let's Encrypt.
- **Autenticación por usuario** — credenciales gestionadas con `htpasswd`; soporta múltiples usuarios con permisos diferenciados.
- **Escaneo de vulnerabilidades** — análisis automático de imágenes con Trivy, integrable como gate obligatorio en pipelines CI/CD.
- **Garbage collection** — limpieza de blobs huérfanos para liberar espacio en disco, ejecutable en modo dry-run.
- **Backups automatizados** — respaldo de configuración y datos de imágenes con soporte para programación vía cron.
- **Monitoreo con Prometheus** — métricas de latencia, tasa de errores y volumen de operaciones disponibles por PromQL.
- **Hardening de seguridad** — contenedores no-root, filesystem read-only, red backend aislada, sin redirects externos a MinIO, y rate limiting por IP.
- **Integración CI/CD lista** — ejemplos para GitLab CI, GitHub Actions y Kubernetes (`imagePullSecret`).
- **Gestión del ciclo de vida de imágenes** — eliminación controlada de manifiestos mediante API v2 con flujo de habilitación/rehabilitación de borrado.

> **Regla de oro:** nunca usar `latest` en producción. Siempre etiquetar imágenes con versión semántica (`app:1.4.2`) o el SHA del commit (`app:abc1234`).

---

## Tabla de contenidos

1. [Stack](#stack)
2. [Requisitos](#requisitos)
3. [Inicio rápido](#inicio-rápido)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Configuración de dominio](#configuración-de-dominio)
6. [Certificados TLS](#certificados-tls)
7. [Gestión de usuarios](#gestión-de-usuarios)
8. [Operaciones del ciclo de vida](#operaciones-del-ciclo-de-vida)
9. [Eliminación de imágenes](#eliminación-de-imágenes)
10. [Consulta del catálogo](#consulta-del-catálogo)
11. [Monitoreo](#monitoreo)
12. [Seguridad](#seguridad)
13. [Integración CI/CD](#integración-cicd)
14. [Evolución del stack](#evolución-del-stack)
15. [Comandos útiles](#comandos-útiles)
16. [Resolución de problemas](#resolución-de-problemas)

---

## Stack

| Componente | Imagen | Rol |
|---|---|---|
| **nginx** | `nginx:1.27-alpine` | Proxy inverso, TLS termination, rate limiting |
| **registry** | `registry:2.8` | Docker Registry API v2 |
| **minio** | `minio/minio:latest` | Storage S3-compatible (capas de imágenes) |
| **prometheus** | `prom/prometheus:latest` | Métricas internas |
| **trivy** | `aquasec/trivy:latest` | Escaneo de vulnerabilidades (on-demand) |

> **Nota sobre versiones:** anclar siempre las imágenes a versiones concretas (ej. `registry:2.8.3`) en producción para garantizar reproducibilidad. Usar `latest` solo en desarrollo.

---

## Requisitos

- Linux (Ubuntu 22.04+ / Debian 12+ / RHEL 9+)
- Docker Engine 26+
- Docker Compose v2 — verificar con `docker compose version`
- OpenSSL 3+
- Dominio o IP accesible desde los clientes Docker
- Mínimo 2 GB RAM, 20 GB disco libre para el almacenamiento de imágenes

---

## Inicio rápido

```bash
# 1. Clonar este repositorio en el servidor
git clone <repo> docker-registry && cd docker-registry

# 2. Bootstrap: genera .env con secretos aleatorios, certificado TLS y htpasswd
chmod +x scripts/*.sh
bash scripts/setup.sh

# 3. Revisar y ajustar .env (dominio, puertos, credenciales)
nano .env

# 4. Levantar el stack
docker compose up -d

# 5. Verificar health de todos los servicios
docker compose ps

# 6. Comprobar que el registry responde (HTTP 401 = correcto, exige autenticación)
curl -sk https://registry.miempresa.com/v2/
# → {"errors":[{"code":"UNAUTHORIZED",...}]}  ✓

# 7. Login y primer push de prueba
docker login registry.miempresa.com
docker pull hello-world
docker tag hello-world registry.miempresa.com/hello-world:1.0
docker push registry.miempresa.com/hello-world:1.0
```

---

## Estructura del proyecto

```
docker-registry/
├── .env.example           ← template de variables (copiar a .env, nunca commitear .env)
├── .gitattributes         ← LF line endings para scripts
├── .gitignore             ← excluye .env, certs/*.key, auth/htpasswd
├── docker-compose.yml     ← orquestación de servicios
│
├── config/
│   ├── nginx/
│   │   └── nginx.conf     ← proxy inverso, TLS 1.2/1.3, rate limiting, security headers
│   ├── registry/
│   │   └── config.yml     ← registry v2: storage, auth, métricas, redirect desactivado
│   └── prometheus/
│       └── prometheus.yml ← scrape de métricas del registry
│
├── auth/
│   └── htpasswd           ← credenciales bcrypt (generado por setup.sh, NO en git)
│
├── certs/
│   ├── registry.crt       ← certificado TLS público (o fullchain de Let's Encrypt)
│   └── registry.key       ← clave privada (600, NO en git)
│
├── backups/               ← destino de backups (creado automáticamente)
│
├── scripts/
│   ├── setup.sh           ← bootstrap inicial (idempotente)
│   ├── gc-registry.sh     ← garbage collection de blobs huérfanos
│   ├── backup-registry.sh ← backup de datos y configuración
│   └── scan-image.sh      ← escaneo de vulnerabilidades con Trivy
│
└── .ci/
    ├── gitlab-ci.yml.example      ← GitLab CI: build → push → scan
    ├── github-actions.yml.example ← GitHub Actions
    └── k8s-secret.yml.example     ← Kubernetes imagePullSecret
```

---

## Configuración de dominio

Editar `REGISTRY_DOMAIN` en `.env` **antes** de ejecutar `setup.sh`:

```bash
REGISTRY_DOMAIN=registry.miempresa.com
```

`setup.sh` aplica el dominio automáticamente al `server_name` de nginx y al CN/SAN del certificado. Si el dominio cambia posteriormente, regenerar los certificados y reiniciar nginx.

---

## Certificados TLS

### Desarrollo / Red interna (self-signed)

`setup.sh` genera el certificado automáticamente con SAN para el dominio configurado, `localhost` y `127.0.0.1`. Sin este paso, `docker login` fallará porque el cliente rechazará el certificado.

#### Linux (Docker Engine)

```bash
# Ejecutar en cada máquina cliente
sudo mkdir -p /etc/docker/certs.d/registry.miempresa.com
sudo scp user@registry-host:~/docker-registry/certs/registry.crt \
     /etc/docker/certs.d/registry.miempresa.com/ca.crt
sudo systemctl reload docker
```

#### Docker Desktop (Windows)

Docker Desktop en Windows usa el **Certificate Store del sistema**, no `/etc/docker/certs.d/`. Son necesarios dos pasos:

**1. Copiar el certificado desde el servidor Ubuntu** (PowerShell):

```powershell
scp <usuario>@<ip-server>:<ruta docker>/docker-registry/certs/registry.crt C:\<ruta colocar>\registry.crt
```

**2. Instalar en el Certificate Store de Windows** (PowerShell como Administrador):

```powershell
Import-Certificate -FilePath "C:\Temp\registry.crt" -CertStoreLocation Cert:\LocalMachine\Root
```

**3. Instalar también en el directorio de Docker Desktop** (complementario):

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.docker\certs.d\10.0.1.61"
Copy-Item "C:\Temp\registry.crt" "$env:USERPROFILE\.docker\certs.d\10.0.1.61\ca.crt"
```

**4. Reiniciar Docker Desktop** — obligatorio para que el daemon recoja el nuevo certificado:

Bandeja del sistema → Docker Desktop → **Restart**

**5. Verificar:**

```powershell
docker login 10.0.1.61 -u admin
```

> El paso crítico es el `Import-Certificate` en `Cert:\LocalMachine\Root`. Sin él, Docker Desktop ignora el directorio `certs.d` y sigue rechazando el certificado self-signed.

#### macOS / WSL2

```bash
mkdir -p ~/.docker/certs.d/registry.miempresa.com
cp certs/registry.crt ~/.docker/certs.d/registry.miempresa.com/ca.crt
```

> En WSL2, ejecutar los comandos dentro de la sesión WSL donde corre Docker Engine.

### Producción (Let's Encrypt — recomendado)

```bash
# Instalar certbot
apt install certbot

# Obtener certificado (puerto 80 debe estar libre temporalmente)
certbot certonly --standalone -d registry.miempresa.com \
  --email admin@miempresa.com --agree-tos --non-interactive

# Copiar al directorio del proyecto
cp /etc/letsencrypt/live/registry.miempresa.com/fullchain.pem certs/registry.crt
cp /etc/letsencrypt/live/registry.miempresa.com/privkey.pem   certs/registry.key
chmod 600 certs/registry.key

# Habilitar OCSP stapling en nginx.conf y reiniciar
# (descomentar las líneas ssl_stapling en config/nginx/nginx.conf)
docker compose restart nginx

# Renovación automática (cron semanal)
echo "0 3 * * 1 root certbot renew --quiet && \
  cp /etc/letsencrypt/live/registry.miempresa.com/fullchain.pem $(pwd)/certs/registry.crt && \
  cp /etc/letsencrypt/live/registry.miempresa.com/privkey.pem   $(pwd)/certs/registry.key && \
  docker compose -f $(pwd)/docker-compose.yml exec nginx nginx -s reload" \
  | sudo tee /etc/cron.d/certbot-registry
```

---

## Gestión de usuarios

El fichero `auth/htpasswd` usa bcrypt (coste 12). Siempre reiniciar el registry tras modificarlo para que los cambios surtan efecto.

```bash
# Agregar usuario (contraseña se solicita interactivamente)
docker run --rm -it httpd:alpine htpasswd -B auth/htpasswd nuevo_usuario

# Agregar usuario no interactivo (para scripts)
docker run --rm httpd:alpine htpasswd -Bbn nuevo_usuario 'contraseña_segura' >> auth/htpasswd

# Listar usuarios actuales
cut -d: -f1 auth/htpasswd

# Eliminar usuario
sed -i '/^usuario_a_eliminar:/d' auth/htpasswd

# Aplicar cambios
docker compose restart registry
```

> **Buena práctica:** crear un usuario de solo lectura por equipo/proyecto en lugar de compartir el usuario `admin`.

---

## Operaciones del ciclo de vida

### Garbage Collection

Elimina blobs no referenciados por ningún manifiesto. Ejecutar regularmente para liberar espacio en MinIO.

> El registry se pone en modo read-only durante la GC. Planificar en ventanas de bajo tráfico.

```bash
# Dry-run (muestra qué se eliminaría sin borrar nada)
bash scripts/gc-registry.sh --dry-run

# Ejecución real
bash scripts/gc-registry.sh

# Programar semanalmente (domingo a las 2 AM)
echo "0 2 * * 0 root /opt/registry/scripts/gc-registry.sh >> /var/log/registry-gc.log 2>&1" \
  | sudo tee /etc/cron.d/registry-gc
```

### Backup

```bash
# Backup completo (config + datos MinIO)
bash scripts/backup-registry.sh

# Solo configuración y credenciales (sin los datos de imágenes)
bash scripts/backup-registry.sh --config-only

# Programar diariamente a las 3 AM
echo "0 3 * * * root /opt/registry/scripts/backup-registry.sh >> /var/log/registry-backup.log 2>&1" \
  | sudo tee /etc/cron.d/registry-backup
```

> **Buena práctica:** copiar los backups a un almacenamiento externo (S3, NFS, servidor remoto) con `rsync` o `rclone` tras cada ejecución.

### Escaneo de vulnerabilidades

```bash
# Escanear imagen (severidad HIGH y CRITICAL por defecto)
bash scripts/scan-image.sh registry.miempresa.com/myapp:1.0

# Solo CRITICAL
bash scripts/scan-image.sh registry.miempresa.com/myapp:1.0 CRITICAL

# Salida JSON (para ingestión en CI/CD o SIEM)
bash scripts/scan-image.sh registry.miempresa.com/myapp:1.0 HIGH,CRITICAL json
```

> Integrar el escaneo como gate obligatorio en el pipeline CI/CD para bloquear despliegues con vulnerabilidades críticas no resueltas.

---

## Eliminación de imágenes

> **Atención:** la eliminación está deshabilitada por defecto (`storage.delete.enabled: false` en `config/registry/config.yml`) como control de seguridad para evitar borrados accidentales. Seguir el procedimiento completo: habilitar → borrar manifiesto → rehabilitar → GC.

El borrado en la Registry API v2 opera sobre **manifiestos** (no sobre tags). El flujo obligatorio es:

1. **Habilitar la API de borrado** en `config/registry/config.yml`
2. **Obtener el digest** del manifiesto a eliminar
3. **Enviar el DELETE** al endpoint del manifiesto
4. **Deshabilitar de nuevo** la API de borrado
5. **Ejecutar Garbage Collection** para liberar el espacio en disco

### Paso 1 — Habilitar la API de borrado

Editar `config/registry/config.yml`:

```yaml
storage:
  delete:
    enabled: true   # cambiar de false a true
```

Reiniciar el registry para aplicar el cambio:

```bash
docker compose restart registry
```

### Paso 2 — Obtener el digest del manifiesto

```bash
PASS=$(grep REGISTRY_ADMIN_PASSWORD .env | cut -d= -f2)

# La cabecera Docker-Content-Digest contiene el hash sha256
curl -sk -u admin:$PASS \
  -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
  -I https://registry.miempresa.com/v2/myapp/manifests/1.0 \
  | grep -i Docker-Content-Digest
# → Docker-Content-Digest: sha256:abc123...
```

### Paso 3 — Eliminar el manifiesto

```bash
DIGEST="sha256:abc123..."   # reemplazar con el valor obtenido en el paso anterior

curl -sk -u admin:$PASS \
  -X DELETE \
  https://registry.miempresa.com/v2/myapp/manifests/${DIGEST}
# HTTP 202 Accepted = eliminación aceptada ✓
```

> Eliminar el manifiesto hace que el tag deje de ser resoluble, pero los blobs (capas) permanecen en disco hasta que se ejecute Garbage Collection.

### Paso 4 — Deshabilitar de nuevo la API de borrado

Restaurar `storage.delete.enabled: false` en `config/registry/config.yml` y reiniciar:

```bash
docker compose restart registry
```

### Paso 5 — Ejecutar Garbage Collection

Este paso libera el espacio en disco ocupado por las capas huérfanas. El registry entra en modo read-only durante la operación.

```bash
# Dry-run primero para verificar qué se eliminará
bash scripts/gc-registry.sh --dry-run

# Ejecución real
bash scripts/gc-registry.sh
```

### Eliminar todos los tags de un repositorio

Si se necesita eliminar un repositorio completo, repetir los pasos 2 y 3 para cada tag:

```bash
PASS=$(grep REGISTRY_ADMIN_PASSWORD .env | cut -d= -f2)
REPO="myapp"

# Listar todos los tags del repositorio
TAGS=$(curl -sk -u admin:$PASS https://registry.miempresa.com/v2/${REPO}/tags/list \
  | python3 -c "import sys,json; print(*json.load(sys.stdin)['tags'], sep=' ')")

# Eliminar manifiesto de cada tag
for TAG in $TAGS; do
  DIGEST=$(curl -sk -u admin:$PASS \
    -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
    -I "https://registry.miempresa.com/v2/${REPO}/manifests/${TAG}" \
    | awk '/docker-content-digest/i {print $2}' | tr -d $'\r')
  echo "Eliminando ${REPO}:${TAG} (${DIGEST})"
  curl -sk -u admin:$PASS -X DELETE \
    "https://registry.miempresa.com/v2/${REPO}/manifests/${DIGEST}"
done

# Ejecutar GC al finalizar
bash scripts/gc-registry.sh
```

---

## Consulta del catálogo

```bash
PASS=$(grep REGISTRY_ADMIN_PASSWORD .env | cut -d= -f2)

# Todos los repositorios almacenados
curl -sk -u admin:$PASS https://registry.miempresa.com/v2/_catalog

# Tags de una imagen concreta
curl -sk -u admin:$PASS https://registry.miempresa.com/v2/myapp/tags/list

# Manifiesto de un tag (metadatos, capas, digest)
curl -sk -u admin:$PASS \
  -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
  https://registry.miempresa.com/v2/myapp/manifests/1.0
```

Respuestas esperadas:

```json
{"repositories":["hello-world","myapp"]}
{"name":"myapp","tags":["1.0","1.1","2.0"]}
```

---

## Monitoreo

Prometheus extrae métricas del registry en `registry:5001/metrics` (red interna únicamente). El endpoint está bloqueado externamente por nginx.

```bash
# Ver métricas en bruto desde dentro de la red Docker
docker compose exec prometheus wget -qO- http://registry:5001/metrics | head -30
```

### Queries PromQL recomendadas

```promql
# Latencia p99 de push de blobs
histogram_quantile(0.99, rate(registry_storage_action_seconds_bucket{action="PutContent"}[5m]))

# Tasa de errores HTTP 5xx
rate(registry_http_requests_total{code=~"5.."}[5m])

# Volumen total de pulls en la última hora
increase(registry_http_requests_total{method="GET",route="/v2/{name}/manifests/{reference}"}[1h])
```

> Acceder a Prometheus UI en `http://localhost:9090` (solo desde el propio host). Para exponer externamente, añadir un `location` en nginx con `auth_basic`.

### Alertas recomendadas

| Condición | Umbral sugerido |
|---|---|
| Tasa de errores 5xx | > 1% durante 5 min |
| Latencia p99 de push | > 30 s |
| Espacio libre en disco | < 20% |
| Contenedor caído | `up == 0` durante 1 min |

---

## Seguridad

### Decisiones de hardening aplicadas

| Control | Implementación |
|---|---|
| TLS 1.2/1.3 only | `ssl_protocols TLSv1.2 TLSv1.3` en nginx.conf |
| HSTS | `Strict-Transport-Security: max-age=63072000; includeSubDomains` |
| Sin borrado por API | `storage.delete.enabled: false` en config.yml |
| Sin redirects a MinIO | `storage.redirect.disable: true` — evita exponer la URL interna de MinIO al cliente |
| Contenedores no-root | `user: 999:999` (registry), `65534:65634` (prometheus) |
| Filesystem read-only | `read_only: true` en registry |
| Privilegios bloqueados | `no-new-privileges: true` en todos los servicios |
| Red backend aislada | `internal: true` — MinIO sin acceso a internet |
| Rate limiting | push: 20 r/m, pull: 120 r/m, conexiones: 20/IP |
| Métricas internas | puerto 5001 bloqueado por nginx desde exterior |
| Secretos en `.env` | Nunca en docker-compose.yml ni config.yml |

### Firewall (UFW)

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 443/tcp
sudo ufw allow 80/tcp   # solo para Let's Encrypt HTTP-01 challenge; bloquear tras renovar
sudo ufw enable
sudo ufw status verbose
```

### Rotación periódica de credenciales

Rotar credenciales al menos cada 90 días o inmediatamente ante una brecha.

```bash
# Generar nueva contraseña segura
NEW_PASS=$(openssl rand -base64 20 | tr -d '/+=')

# Actualizar htpasswd (escritura atómica)
docker run --rm httpd:alpine htpasswd -Bbn admin "${NEW_PASS}" > auth/htpasswd.new
mv auth/htpasswd.new auth/htpasswd
chmod 600 auth/htpasswd

# Actualizar .env
sed -i "s/^REGISTRY_ADMIN_PASSWORD=.*/REGISTRY_ADMIN_PASSWORD=${NEW_PASS}/" .env

# Reiniciar registry para que lea el nuevo htpasswd
docker compose restart registry

echo "Nueva contraseña: ${NEW_PASS}"  # guardar en gestor de secretos
```

### Buenas prácticas adicionales

- **Auditoría de accesos:** centralizar los logs de nginx en un SIEM (ELK, Splunk, Loki) para detectar patrones anómalos.
- **Mínimo privilegio:** crear un usuario por aplicación/equipo en lugar de usar `admin` globalmente.
- **Imágenes base mínimas:** preferir `distroless` o `alpine` para reducir la superficie de ataque.
- **Firma de imágenes:** integrar [cosign](https://docs.sigstore.dev/cosign/overview/) para verificar la integridad antes de desplegar.
- **SBOMs:** generar Software Bill of Materials con `syft` al publicar cada imagen para trazabilidad de dependencias.

---

## Integración CI/CD

| Plataforma | Archivo | Descripción |
|---|---|---|
| GitLab CI | [`.ci/gitlab-ci.yml.example`](.ci/gitlab-ci.yml.example) | build → scan → push (gate de seguridad) |
| GitHub Actions | [`.ci/github-actions.yml.example`](.ci/github-actions.yml.example) | build → scan → push |
| Kubernetes | [`.ci/k8s-secret.yml.example`](.ci/k8s-secret.yml.example) | imagePullSecret para pull desde k8s |

### Flujo recomendado

```
build → test unitarios → scan Trivy (bloquear si CRITICAL) → push al registry → deploy
```

---

## Evolución del stack

| Necesidad | Solución recomendada |
|---|---|
| UI web + RBAC por proyecto | [Harbor](https://goharbor.io/) |
| Auditoría de accesos centralizada | ELK Stack / Grafana Loki |
| Autenticación empresarial (SSO) | OIDC con Keycloak o Okta |
| Alta disponibilidad | MinIO distribuido + nginx upstream con múltiples backends |
| Firma y verificación de imágenes | [cosign](https://docs.sigstore.dev/) + policy-controller |
| Replicación entre registries | Harbor replication / Skopeo en cron |

---

## Comandos útiles

```bash
# Estado de todos los servicios
docker compose ps

# Logs en tiempo real (todos los servicios)
docker compose logs -f

# Logs de un servicio concreto
docker compose logs -f nginx
docker compose logs -f registry

# Listar repositorios almacenados
curl -sk -u admin:$(grep REGISTRY_ADMIN_PASSWORD .env | cut -d= -f2) \
  https://registry.miempresa.com/v2/_catalog

# Listar tags de una imagen
curl -sk -u admin:$(grep REGISTRY_ADMIN_PASSWORD .env | cut -d= -f2) \
  https://registry.miempresa.com/v2/myapp/tags/list

# Reiniciar un servicio
docker compose restart registry

# Actualizar todas las imágenes del stack y reiniciar
docker compose pull && docker compose up -d

# Validar configuración de nginx sin reiniciar
docker compose exec nginx nginx -t
```

---

## Resolución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `docker push` devuelve `denied` | No hay sesión activa o credenciales incorrectas | `docker login registry.miempresa.com` |
| `docker push` se cuelga o falla silenciosamente | MinIO redirige al cliente a `minio:9000` (no resolvible externamente) | Añadir `redirect: disable: true` en `config/registry/config.yml` y reiniciar el registry |
| `docker login` falla con error TLS (Linux) | Certificado self-signed no instalado en el cliente | Copiar `certs/registry.crt` a `/etc/docker/certs.d/<dominio>/ca.crt` y recargar docker |
| `docker login` falla con error TLS (Windows) | Docker Desktop usa el Certificate Store del sistema, no `certs.d` | `Import-Certificate -FilePath registry.crt -CertStoreLocation Cert:\LocalMachine\Root` como Administrador y reiniciar Docker Desktop |
| HTTP 301 en login (bucle) | Cliente conecta por HTTP en lugar de HTTPS | Usar `https://` explícitamente; verificar que nginx escucha en 443 |
| `401 Unauthorized` persistente tras login correcto | Credenciales en `auth/htpasswd` corruptas o algoritmo incorrecto | Regenerar htpasswd con `htpasswd -Bbn` (bcrypt obligatorio) |
| Registry no arranca | Error de configuración en `config.yml` | `docker compose logs registry` — validar YAML con `yamllint` |
| Espacio en disco agotado | Blobs huérfanos acumulados | Ejecutar `bash scripts/gc-registry.sh` |
