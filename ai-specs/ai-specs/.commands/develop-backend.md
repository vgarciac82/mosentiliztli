Analiza y resuelve la feature de Redmine: $ARGUMENTS.

Sigue estos pasos:

1. Entender el problema descrito en la feature
2. Buscar en el codebase los archivos relevantes
3. Crear un nuevo branch usando la convención del proyecto: `feature/[ticket-id]-backend` (por ejemplo `feature/35-backend`). Verificar si el branch ya existe y, si no, crearlo desde `main` o `develop`.
4. Implementar los cambios necesarios para resolver la feature, siguiendo el orden de las tareas y cumpliendo todas (escribir y ejecutar tests para verificar la solución, actualizar documentación, etc.)
5. Asegurar que el código pase linting y type checking
6. Hacer stage solo de los archivos afectados por la feature y dejar fuera del commit cualquier otro archivo modificado. Crear un mensaje de commit descriptivo en español con referencia al ticket (por ejemplo `#35`).
7. Hacer push y crear un PR enlazado al ticket Redmine referenciando el ID del ticket (por ejemplo `#35`).

Usar la GitHub CLI (`gh`) para todas las tareas relacionadas con GitHub.
