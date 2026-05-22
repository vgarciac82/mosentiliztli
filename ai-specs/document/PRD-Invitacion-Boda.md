# Contexto Maestro del Sistema: Invitación Digital y RSVP (Boda)

## 1. Objetivo y Alcance General
[cite_start]Desarrollar un sistema web integral para la gestión y control de asistencia de un evento masivo[cite: 497]. [cite_start]El sistema debe resolver tres frentes críticos: la automatización del flujo de invitaciones personalizadas, la contención y consistencia de las confirmaciones (RSVP), y la centralización de datos en un panel administrativo[cite: 498, 499, 500, 502].

---

## 2. Arquitectura del Sistema
[cite_start]El sistema se compone de tres módulos desacoplados que interactúan sobre la misma base de datos[cite: 504]:

* [cite_start]**2.1 Módulo Administrativo (Backoffice):** Interfaz protegida por autenticación basada en servidor (MVC) destinada exclusivamente a los organizadores para la gestión analítica y operativa del evento[cite: 506, 507, 641, 642].
* [cite_start]**2.2 Micrositio de Invitación (Vista Invitado):** Aplicación web responsiva de acceso controlado[cite: 206, 254, 508]. Rutas requeridas:
    * [cite_start]`/invitacion/{token}` -> Acceso directo parametrizado mediante token único[cite: 256, 547].
    * `/invitacion` -> Ruta raíz. [cite_start]Si se accede sin token, renderiza un formulario de validación que solicita obligatoriamente el "Código de Invitación"[cite: 257, 258, 262, 548].
* [cite_start]**2.3 API Backend:** Capa de servicios para la persistencia, lógica de negocio y consumo de webhooks/APIs de mensajería (Twilio)[cite: 510, 512, 513, 514].

---

## 3. Modelo de Datos y Estados (PostgreSQL)

### [cite_start]3.1 Diccionario de Tablas Mínimas [cite: 222, 320]
* [cite_start]`event_config`: Parámetros globales del evento (Fecha, capacidad total, configuraciones del sistema)[cite: 333].
* [cite_start]`guest`: Registro centralizado de la unidad de invitación (familia o grupo)[cite: 223, 330, 522].
* [cite_start]`rsvp_response`: Almacenamiento transaccional de las respuestas de confirmación[cite: 224, 324, 331].
* [cite_start]`whatsapp_message`: Historial y log de mensajería enviada/recibida vía Twilio API[cite: 228, 231, 325, 332].
* [cite_start]`song_suggestion`: Registro de canciones propuestas por invitados[cite: 225, 326].
* [cite_start]`guest_message`: Mensajes personales capturados por los invitados destinados a moderación[cite: 388, 389].

### [cite_start]3.2 Campos Requeridos para Carga de Datos (`guest`) [cite: 266, 267, 517]
La importación masiva por layout estructurado debe poblar obligatoriamente:
`Familia (String)` | `pases_asignados (Integer)` | `Celular (String con código de país)` | `NombreInvitado (String)` | `Grupo (String)` | [cite_start]`Notas (Text)`[cite: 268, 271, 342, 519].

El sistema debe autogenerar de forma nativa al insertar el registro:
* [cite_start]`guest_id` (UUID) [cite: 275]
* [cite_start]`token` (Hash alfanumérico seguro para la URL) [cite: 276, 524]
* [cite_start]`codigo_invitacion` (Código corto para validación manual en login) [cite: 277, 524]
* [cite_start]`estatus` = `PENDIENTE` [cite: 278, 335, 644]

### [cite_start]3.3 Máquina de Estados del Invitado (`estatus`) [cite: 334, 643]
Un invitado solo puede transicionar entre los siguientes estados:
1.  [cite_start]`PENDIENTE`: Estado inicial post-carga[cite: 335, 644].
2.  [cite_start]`INVITACION_ENVIADA`: Confirmación de entrega del SMS/WhatsApp por la API proveedora[cite: 336, 645].
3.  [cite_start]`CONFIRMADO`: El invitado completó el flujo web y asistirá (pases_confirmados > 0)[cite: 337, 646].
4.  [cite_start]`DECLINADO`: El invitado completó el flujo web e indicó inasistencia (pases_confirmados = 0)[cite: 338, 647].
5.  [cite_start]`RESPONDIO_WHATSAPP`: Mensaje entrante detectado en webhook fuera del flujo web (requiere atención)[cite: 339, 648].
6.  [cite_start]`ERROR_ENVIO`: Falla reportada por el proveedor de mensajería[cite: 340, 649].

---

## 4. Reglas de Negocio Estrictas

### 4.1 Restricciones de Confirmación (RSVP)
* [cite_start]**Regla de Oro de Pases:** El invitado bajo ninguna circunstancia puede incrementar el número de pases confirmados por encima de los `pases_asignados` configurados en su registro[cite: 309, 557]. [cite_start]El selector gráfico debe topar dinámicamente en el número asignado[cite: 314].
* [cite_start]**Modulación de Pases:** Se permite confirmar una cantidad menor o igual (`pases_confirmados` $\le$ `pases_asignados`)[cite: 558]. [cite_start]Si confirma menos pases de los asignados, el backend debe registrar internamente el estatus del RSVP como una confirmación parcial para el recuento de asientos libres[cite: 74].
* [cite_start]**Acceso Condicionado:** La interacción con los módulos de Interacción (Playlist, Mensajes, Fotos) está condicionada a que el invitado guarde primero su estado como `CONFIRMADO`[cite: 367, 450]. [cite_start]Si el estado es `DECLINADO`, estos módulos se deshabilitan y solo se presenta el campo de texto opcional de despedida[cite: 65, 68, 315, 317].
* **Transiciones de RSVP (Idempotencia y Bloqueo):** Un invitado puede cambiar su respuesta de estado inicial a `CONFIRMADO`, y posteriormente puede arrepentirse y pasar a `DECLINADO`. Sin embargo, una vez en estado `DECLINADO`, el flujo es terminal (solo-lectura) para el invitado. Solo la pareja (desde el Admin) puede hacer un "reset" reenviando la invitación.
* **Expiración (Deadline):** El formulario de RSVP caduca automáticamente a los **35 días** de haberse enviado la invitación. Pasado este tiempo, el acceso para confirmación se bloquea y solo la pareja puede reactivarlo (reset).

### 4.2 Integración Outbound e Inbound con Twilio
* [cite_start]**Outbound (Marketing Templates):** Al tratarse de notificaciones proactivas del sistema, se debe implementar usando plantillas tipo "Marketing" aprobadas por Meta[cite: 281, 282, 528]. [cite_start]El sistema debe inyectar dinámicamente el nombre del invitado y la URL personalizada parametrizada con el token único[cite: 290, 530, 533].
* [cite_start]**Inbound (Bandeja de Entrada Admin):** Dado que los usuarios tienden a responder directamente al canal de WhatsApp en lugar de entrar a la liga, el webhook de entrada debe capturar el texto íntegro, asociarlo al `guest_id` mediante el número telefónico emisor y mapearlo en una interfaz dedicada en el panel administrativo[cite: 294, 569, 571, 572, 573]. [cite_start]Las acciones administrativas manuales sobre estos mensajes entrantes son: *Marcar Confirmado*, *Marcar Declinado*, *Ignorar* o *Responder vía API*[cite: 305, 575, 576, 577, 578, 579].
* **Bandeja de No Identificados:** Si el webhook de Twilio recibe un mensaje de un número de teléfono que **no existe** en la tabla `guest`, el mensaje no se descarta. Se almacenará en una "Bandeja de no identificados" dentro del panel administrativo para su revisión, conciliación manual (enlazarlo a un invitado) o descarte.

### 4.3 Módulo de Moderación y Proyección de Mensajes
* [cite_start]**Flujo de Aprobación:** Los mensajes capturados en la sección "Mensaje a los Novios" (máximo 200 caracteres configurables) ingresan al sistema con una bandera booleana `aprobado = false` y `permitir_proyeccion = false` por defecto[cite: 370, 371, 380, 381, 387, 395]. [cite_start]No se proyectará ningún mensaje de forma automática[cite: 487, 592].
* [cite_start]**Atributos de Almacenamiento:** El objeto debe persistir si la respuesta es anónima (`es_anonimo`) y si el usuario autoriza explícitamente su reproducción en pantalla (`permitir_proyeccion`)[cite: 374, 377, 387, 393, 394].
* [cite_start]**URL de Proyección Independiente:** Se requiere una vista limpia y exclusiva (`/presentacion/mensajes`) optimizada para pantallas completas que consuma únicamente los registros filtrados con `aprobado = true`[cite: 408, 409, 410, 456, 604]. [cite_start]Debe operar bajo un temporizador en loop automático (configurable entre 5 y 8 segundos por registro)[cite: 419, 421, 607].
* [cite_start]**Plan de Contingencia (Modo Offline):** El módulo administrativo debe incluir una función de exportación nativa que compile todos los mensajes aprobados en un archivo HTML ejecutable localmente sin dependencia de conexión a red en el servidor del evento[cite: 436, 437, 439, 440].

---

## 5. Bloques Funcionales del Microsite (Planificación por Fases)

### [cite_start]Fase 1: MVP Operativo (Obligatorio Core) [cite: 357, 651, 900]
1.  [cite_start]**Módulo de Carga:** Importación del layout de datos inicial[cite: 357, 652].
2.  [cite_start]**Motor de Envío:** Integración con Twilio para dispersión de tokens[cite: 357, 653].
3.  [cite_start]**Acceso Controlado e Interfaz Base:** Enrutamiento por token o validación por código[cite: 357, 547, 548, 654].
4.  [cite_start]**Formulario RSVP:** Procesamiento transaccional de asistencia con bloqueo de pases excedentes[cite: 357, 557, 655].
5.  [cite_start]**Dashboard de Control:** Vista analítica con indicadores clave en tiempo real[cite: 357, 656]:
    * [cite_start]*Métricas globales:* Total de invitados cargados, mensajes enviados con éxito, errores de envío y pendientes[cite: 346, 347, 348].
    * [cite_start]*Métricas de confirmación:* Desglose exacto de invitados Confirmados, Declinados y Sin Respuesta[cite: 349, 350, 351].
    * [cite_start]*Métricas de capacidad:* Relación matemática estricta entre `pases_asignados` vs. `pases_confirmados`[cite: 352, 353].
    * [cite_start]*Atención:* Contador de mensajes entrantes de WhatsApp pendientes de revisión manual[cite: 354].

### [cite_start]Fase 2: Módulos de Experiencia e Información [cite: 358, 657, 902]
1.  [cite_start]**Hero de Entrada:** Pantalla de bienvenida con los nombres de los novios, fecha oficial y gatillo de apertura (`scroll` o botón)[cite: 669, 673, 674, 675].
2.  [cite_start]**Cuenta Regresiva:** Componente dinámico frontal que calcule la diferencia temporal exacta en días, horas, minutos y segundos hacia la fecha del evento[cite: 677, 680, 681, 682, 683, 793, 795, 796, 797, 798].
3.  [cite_start]**Detalles del Evento:** Desglose logístico que separe explícitamente la Ceremonia de la Recepción (horas específicas y nombre del recinto) con integración nativa a botones de redirección para Google Maps y Waze[cite: 39, 40, 41, 42, 43, 44, 46, 47, 48, 808, 816, 817].
4.  [cite_start]**Código de Vestimenta (Dress Code):** Espacio informativo para la especificación del protocolo de vestimenta y restricciones de etiqueta[cite: 132, 134, 135].

### [cite_start]Fase 3: Módulos de Interacción Avanzada [cite: 359, 661]
1.  [cite_start]**Playlist Colaborativa (Opción Controlada):** Formulario interno donde el invitado confirmado captura `Nombre de la Canción`, `Artista` y `Dedicatoria (opcional)`[cite: 83, 85, 86, 87, 614, 615, 616]. [cite_start]Estos registros se guardan en estado `PENDIENTE` en la tabla `song_suggestion` hasta que el administrador los marque como *Aceptada* o *Rechazada* desde el panel[cite: 165, 171].
2.  [cite_start]**Mesa de Regalos:** Enlaces externos configurables hacia tiendas departamentales (Amazon, Liverpool) junto con un componente para el "Sobre Digital", mostrando de forma clara los datos bancarios estructurados (Banco, Cuenta, CLABE, Titular) con un botón nativo para copiar la CLABE al portapapeles[cite: 90, 95, 96, 97, 99, 100, 101, 102, 103, 104, 620, 621].
3.  [cite_start]**Captura de Mensajes a Novios:** Activación del formulario de recopilación para el sistema de proyección del vals[cite: 365, 366, 658].
4.  [cite_start]**Módulo de Carga Multimedia (Fase Futura):** Redirección integrada a repositorios compartidos en la nube (Google Photos / Google Drive) como almacenamiento externo temporal de imágenes del evento[cite: 111, 112, 113, 625, 626, 747, 748].

---

## 6. Reglas de UI/UX Estáticas e Inmutables (No Modificar)
* [cite_start]**Estructura Visual:** El microsite se despliega estrictamente como una única página vertical (Single Page Application / Vertical Scroll) de alta fluidez en dispositivos móviles[cite: 118, 206].
* **Identidad Gráfica Aplicada:** Los estilos visuales, paletas de colores, fuentes tipográficas y assets multimedia ya están definidos y validados en el frontend actual. **Cualquier sugerencia de diseño generada por la IA debe ser omitida**. Los esfuerzos de codificación se centrarán exclusivamente en la lógica funcional, reactividad de los componentes, consistencia del estado del cliente y apego estricto a las reglas de negocio descritas en este documento.