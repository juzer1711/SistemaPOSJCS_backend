# Instrucciones backend POS

Esta aplicación usa Java 21, Spring Boot y PostgreSQL. Las versiones exactas están en pom.xml, Maven Wrapper y Docker Compose.

Leer la [skill backend-pos-architect](.cursorrules/backend-pos-architect/SKILL.md) para implementación/refactor y el [README](README.md) para entorno y contratos. Estas instrucciones son autosuficientes para la futura separación.

- Trabajar por alcance aprobado y conservar el worktree. Diagnosticar primero rutas, DTOs, entidades, servicios, consultas y permisos.
- Organizar código nuevo bajo modules/<dominio> con controller, dto, model, repository y service según necesidad. No trasladar todo el legado sin solicitud.
- Responder con DTOs; no filtrar entidades JPA ni hashes de contraseña a la API.
- Mantener contratos y consumidores frontend. No imponer una restricción general de frontend solo lectura si el usuario aprueba una tarea de ambas aplicaciones.
- Autenticación JWT Bearer y BCrypt; autorización mediante reglas usuario-módulo, sin roles.
- Aplicar permisos en backend, no confiar en botones ocultos. Activar/desactivar usuarios exige ACTUALIZACION; exportaciones de usuarios exigen REPORTE.
- Validar entrada, unicidad e invariantes en servidor. Documento de usuario y código de módulo son inmutables al editar.
- Paginar y ordenar en servidor con campos permitidos, límites y desempate estable. Medir antes de añadir índices; evitar N+1 y cargas de credenciales en listados.
- Elegir transacciones, bloqueos y operaciones parciales según concurrencia real. Conservar permisos ajenos al modificar una casilla.
- No registrar secretos ni datos reales de autenticación. No ejecutar SQL destructivo o restaurar bases como parte de una limpieza.
- Usar Maven Wrapper y pruebas pertinentes; git diff --check. Distinguir mocks de pruebas reales de PostgreSQL y flujos autenticados.
- Documentar endpoints y ejemplos sintéticos cuando cambie el contrato; no es necesario adjuntar una colección Postman en toda respuesta.
- Mantener documentación vigente y no afirmar que una migración manual mencionada en una conversación está aplicada o disponible en el repositorio.
