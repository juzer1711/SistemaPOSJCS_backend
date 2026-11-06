---
name: backend-pos-architect
description: Implementar o refactorizar el backend POS Spring Boot por dominios, conservando contratos, permisos dinámicos y consultas mantenibles.
---

# Refactor backend POS

## Preparación y alcance

Leer [AGENTS](../../AGENTS.md) y [README](../../README.md). Inspeccionar cambios de Git y código real: controller, DTO, entidad, servicio, repositorio, autorización y consumidores frontend.

El usuario guía prioridades y comportamiento. Diagnosticar y proponer cuando lo pida; implementar el alcance ya aprobado sin pedirlo otra vez. No imponer una prohibición permanente de editar frontend si la tarea autoriza ambas aplicaciones. Conservar el resto del worktree.

## Arquitectura

La base actual usa Java 21, Spring Boot y PostgreSQL; consultar pom.xml para versiones. Código modular en modules/<dominio>:

- controller: contrato HTTP, parámetros y validación.
- dto: entradas, salidas y proyecciones públicas.
- model: entidades JPA.
- repository: persistencia y consultas.
- service: reglas, invariantes, transacciones y coordinación.

No exponer entidades directamente. No renombrar todos los DTO para imponer un sufijo nuevo: seguir convenciones existentes. Centralizar traducción de errores en GlobalExceptionHandler y preservar contratos HTTP.

Dominios modularizados: autenticacion, autorizacion, empresa, modulos, reglas y usuarios. Mantener el estado y pendientes en README. El resto del legado se adapta por solicitudes.

## Procedimiento por entidad

1. Inventariar operaciones, filtros, campos de sort, asociaciones, exportaciones y consumidores.
2. Verificar validaciones de creación/edición, unicidad, campos inmutables y errores.
3. Revisar consultas y planes representativos; detectar N+1, relaciones innecesarias, COUNT costoso y carga de credenciales.
4. Usar proyecciones/DTO cuando reduzcan carga real. Mantener Page si la UI necesita totales.
5. Limitar tamaños, aceptar solo sort permitido y agregar desempate estable.
6. Ubicar transacciones en servicio y decidir bloqueos/actualizaciones atómicas según concurrencia e invariantes.
7. Aplicar autorización backend y comprobar operaciones especiales como estado y exportación.
8. Probar y retirar legacy solo tras verificar referencias y paridad. Documentar contratos afectados.

Ejemplo: usuarios lista mediante UsuarioVista sin seleccionar password, admite sort público con ID de desempate y recorre exportaciones por keyset. No sustituir todo por findAll para filtrar en memoria.

## Autenticación y autorización

- JWT Bearer con firma, expiración, usuario activo y revocación; hashes BCrypt.
- No cambiar transporte a cookies ni introducir roles. No quitar autenticación para resolver un 401.
- CORS y SecurityFilterChain centralizados. No añadir @CrossOrigin dispersos.
- @ModuloProtegido vincula código de módulo; @PermisoRequerido permite excepciones al mapeo por verbo.
- CONSULTA, INSERCION, ACTUALIZACION, REPORTE y DELETE son independientes. Ausencia de concesión deniega.
- Activar/desactivar usuarios usa ACTUALIZACION; exportar usuarios usa REPORTE.
- /api/auth/permisos devuelve permisos propios, no acepta seleccionar otra identidad.
- Administrar reglas permite afectar otros usuarios y autorrevocarse; no agregar privilegios implícitos.

## Invariantes existentes

- Documento de usuario y codigoModulo no cambian al editar, aunque se manipule el payload.
- Contraseña opcional al editar; creación obligatoria y límite BCrypt de 72 bytes UTF-8. Nunca registrar valores o hashes en auditoría.
- Capturar operador antes de cambiar su propio username/estado; frontend cierra sesión después.
- Reglas nuevas en false mediante inserción masiva idempotente. No sobrescribir concesiones existentes.
- Una casilla de matriz actualiza solo su permiso con CASE; ON CONFLICT materializa relaciones ausentes y conserva cambios concurrentes.
- Una empresa por instalación; bootstrap protegido con token y logos Cloudinary bajo namespace propio.
- No confundir búsqueda parcial de usuarios con prefijos de módulos/selectores. Escapar comodines literales.

## Rendimiento, datos y validación

No añadir índices por intuición: medir con EXPLAIN (ANALYZE, BUFFERS), considerando filtros, sort y conteo. PostgreSQL es el motor actual; no prometer portabilidad de SQL nativo ON CONFLICT.

Exportaciones de usuarios: keyset de 500 y SXSSF de 100 filas; el byte[] final sigue en memoria. No describirlas como streaming HTTP completo.

ddl-auto=update no retira columnas/FK antiguas. No afirmar migraciones aplicadas ni citar scripts inexistentes. No usar bases reales para datos de prueba ni eliminar volúmenes como limpieza.

Usar .\mvnw.cmd test o clean verify y pruebas específicas cuando corresponda; git diff --check. UsuarioRepositoryPostgresTest requiere variables POS_TEST_DB_* y un esquema aislado. Los secretos se suministran fuera del código y no se imprimen.

Identificar por separado unitarias, MockMvc, consultas reales PostgreSQL y HTTP autenticado real. Entregar ejemplos sintéticos de Postman cuando ayuden a verificar un contrato, no un bloque obligatorio de URLs inventadas en toda respuesta.
