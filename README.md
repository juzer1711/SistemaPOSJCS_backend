# Backend Sistema POS

API REST en Java 21 y Spring Boot 4.1.2 con JPA/Hibernate, Spring Security y PostgreSQL 17. Maven Wrapper fija Maven 3.9.11; pom.xml es la fuente de versiones.

## Entorno y ejecución

Requisitos: JDK 21, Docker Desktop/Compose para PostgreSQL local y credenciales Cloudinary para los flujos de logo. Gemini puede deshabilitarse.

Desde esta carpeta:

```powershell
# Solo si todavía no existe .env
Copy-Item .env.example .env
docker compose up -d
docker compose ps
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

No sobrescribir un .env existente. Completar DB_URL (JDBC PostgreSQL), DB_USERNAME, DB_PASSWORD, POSTGRES_DB y POSTGRES_PORT coherentemente. La plantilla tiene valores vacíos: no proporciona una base preconfigurada. El servidor usa el puerto 8080; PostgreSQL se publica solo en 127.0.0.1.

application.properties importa opcionalmente .env desde el directorio de ejecución. Configurar también:

- JWT_SECRET: secreto aleatorio de al menos 32 bytes; JWT_EXPIRATION controla duración.
- CORS_ALLOWED_ORIGINS: orígenes exactos separados por coma. Vite solicita http://localhost:3000; comprobar el puerto efectivo.
- CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET para logos.
- EMPRESA_BOOTSTRAP_TOKEN: secreto para el alta inicial; no incluir en el bundle frontend.
- EMPRESA_LOGO_NAMESPACE: identificador único por instalación que comparta Cloudinary.
- GEMINI_ENABLED=false si no se usa el asistente; no es necesario activar Gemini para probar CRUD.

No documentar valores reales ni versionar .env o respaldos. Cambiar JWT_SECRET invalida tokens anteriores.

## Persistencia y SQL

PostgreSQL mantiene datos en el volumen postgres_data de Docker. stop detiene sin borrar; down conserva el volumen; down -v borra la base local. No usar eliminación de volúmenes como limpieza del proyecto.

Hibernate usa ddl-auto=update: crea/actualiza estructuras, pero no sustituye migraciones controladas ni elimina automáticamente columnas antiguas. spring.sql.init.mode=always está configurado; no asumir que por ello se aplican scripts ubicados fuera de los recursos de Spring.

El script versionado [sql/modulos-indices.sql](sql/modulos-indices.sql) crea el índice único de nombre de módulo normalizado. Revisar duplicados y aplicar explícitamente cuando corresponda. No existe en esta fuente un scripts/modulos_seed.sql ni una colección Postman versionada.

Las migraciones históricas de retiro de roles, incorporación de codigo_modulo y restricciones de empresa se discutieron fuera de los archivos actuales. Antes de usar una base antigua, inspeccionar su esquema y preparar una migración revisable; no inventar ni asumir su aplicación. El primer usuario y sus permisos administrativos requieren un procedimiento explícito; el registro de empresa no es un alta automática de administrador.

No se copian datos de Supabase al iniciar el contenedor. Una base nueva y una base histórica pueden requerir preparación diferente.

## Arquitectura

Código modular bajo src/main/java/com/sistemaposjcs/sistemaposjcs/modules/:

| Paquete | Responsabilidad |
| --- | --- |
| autenticacion | Login, identidad, logout, JWT y revocación. |
| autorizacion | Reglas de acceso, anotaciones e interceptor. |
| empresa | Registro inicial, perfil y logos. |
| modulos | Catálogo de módulos y disponibilidad de código. |
| reglas | Relación usuario-módulo, matriz y actualización parcial de permisos. |
| usuarios | CRUD, filtros, catálogo de documentos, estado y exportación. |

Cada dominio distribuye responsabilidades en controller/, dto/, model/, repository/ y service/ según necesidad. Los controladores devuelven DTOs; los servicios controlan invariantes y transacciones; repositorios implementan persistencia/proyecciones. La configuración común permanece en config y los errores se traducen en GlobalExceptionHandler.

Clientes, productos, ventas, cajas, inventario, reportes y auditoría conservan código fuera de los dominios nuevos. No considerar finalizada su refactorización ni moverlo masivamente.

## Autenticación y permisos

| Método/ruta | Comportamiento |
| --- | --- |
| POST /api/auth/login | Devuelve JWT en token. |
| GET /api/auth/me | Identidad autenticada. |
| GET /api/auth/permisos | Solo permisos del autenticado; no admite seleccionar otra identidad. |
| POST /api/auth/logout | Revoca el token actual hasta su expiración. |

Usar Authorization: Bearer en solicitudes privadas; no cookies ni roles como mecanismo de autorización. BCrypt almacena hashes; no se devuelven contraseñas ni hashes. El JWT identifica por username y valida usuario activo. La revocación usa jwt_revocados y limpia entradas expiradas al revocar otro token.

Los públicos de esta fase son login, GET /api/empresa y POST /api/empresa (este último exige token de instalación y empresa ausente). CORS se centraliza en SecurityConfig. No añadir @CrossOrigin por controlador ni quitar autenticación para resolver un 401.

@ModuloProtegido y @PermisoRequerido aplican permisos CONSULTA, INSERCION, ACTUALIZACION, REPORTE y DELETE. Los permisos son independientes; ausencia de regla o de concesión deniega acceso. Se aplican a usuarios, módulos, reglas, escrituras de empresa y exportaciones de usuarios. Otros endpoints requieren JWT pero todavía no están todos adaptados a Regla.

## Contratos de usuarios

| Método/ruta | Uso |
| --- | --- |
| GET /api/users | Activos paginados. |
| GET /api/users/inactivos | Inactivos paginados. |
| GET /api/users/search | Filtros search, estado y tipoDocumento; page, size y sort. |
| GET /api/users/{id} | Consulta individual. |
| GET /api/users/tipos-documento | Valores del enum vigente. |
| POST /api/users | Crear; contraseña requerida. |
| PUT /api/users/{id} | Editar; documento inmutable y contraseña opcional. |
| PATCH /api/users/activar/{id} | ACTUALIZACION; devuelve usuario. |
| PATCH /api/users/desactivar/{id} | ACTUALIZACION; respuesta 204. |
| GET /api/export/usuarios/excel o /csv | REPORTE; mismos filtros de búsqueda. |

search admite coincidencias parciales por nombre completo, username, documento y correo, sin distinguir mayúsculas; escapa comodines literales y limita a 150 caracteres. El sort admite campos públicos definidos en UsuarioService y añade ID como desempate. nombreCompleto se traduce a campos persistidos. La paginación común usa 20 por defecto y máximo 100; la UI puede solicitar 10.

Los listados usan UsuarioVista sin cargar el hash. Las escrituras de actualización/estado bloquean la fila en su transacción. Capturar al operador antes de cambiar username/estado propios permite registrar la auditoría; el frontend cierra sesión tras esos cambios propios. Las contraseñas se validan también por límite BCrypt de 72 bytes UTF-8.

Las exportaciones recorren ventanas keyset de 500, sin COUNT por lote; Excel usa SXSSF con ventana de 100 filas. La respuesta final sigue siendo byte[] en memoria: no es streaming HTTP ilimitado.

## Módulos

GET /api/modulos y /search listan con filtros de prefijo por nombre/zona y orden controlado; GET /{idModulo} consulta; POST crea y PUT /{idModulo} edita. GET /api/modulos/disponibilidad-codigo comprueba disponibilidad.

codigoModulo es textual, único e inmutable y vincula permisos. La UI no ofrece eliminar, aunque el backend conserva DELETE /api/modulos/{idModulo}. La disponibilidad anticipada ayuda al formulario; la validación final de unicidad sigue siendo del servidor.

## Matriz de reglas

| Método/ruta | Permiso y función |
| --- | --- |
| GET /api/reglas/matriz | CONSULTA REGLAS; usuarioId obligatorio, moduloId opcional, paginación. |
| GET /api/reglas/usuarios | CONSULTA REGLAS; selector por prefijo username, incluye activos/inactivos. |
| GET /api/reglas/modulos | CONSULTA REGLAS; selector por prefijo nombre/código. |
| PATCH /api/reglas/permisos | ACTUALIZACION REGLAS; guarda una casilla y responde 204. |

Ejemplo sintético de PATCH:

```json
{ "usuarioId": 7, "moduloId": 3, "accion": "CONSULTA", "valor": false }
```

El CRUD previo de /api/reglas se conserva; PUT reemplaza sus valores completos. La matriz usa proyecciones y LEFT JOIN: una relación ausente aparece en false. PATCH materializa la relación con ON CONFLICT DO NOTHING y usa CASE para conservar permisos ajenos a la casilla ante cambios concurrentes.

Crear usuarios/módulos preasigna reglas mediante INSERT ... SELECT con permisos false, sin sobrescribir existentes. La combinación usuario-módulo es única. Estas consultas son específicas de PostgreSQL. El usuario puede revocar sus propios permisos; no existe una excepción oculta para administrador.

## Empresa

Una empresa por instalación. GET /api/empresa es público para personalización; POST requiere X-Empresa-Setup-Token mientras no exista empresa. PUT /api/empresa/{idEmpresa} y POST /api/empresa/logo requieren ACTUALIZACION EMPRESA.

La acción del POST de logo se declara explícitamente como ACTUALIZACION: no exige INSERCION ni CONSULTA. La consulta pública no adquiere permisos administrativos. El frontend protege el acceso a su formulario con CONSULTA y las acciones con ACTUALIZACION; esto no restringe el contenido del GET público.

Las escrituras de datos y logo usan bloqueo pesimista de la misma fila para serializar solicitudes concurrentes de distintas sesiones y preservar el logo/datos ajenos a cada operación. Esto no detecta borradores antiguos de campos editables: entre dos PUT completos prevalece el último que se aplica.

La respuesta de ambas escrituras es EmpresaResponse completa. El PUT acepta los campos de EmpresaRequest, incluido `responsableIva`; no modifica logo ni estado. El logo recibe multipart `file`; elimina el anterior después del commit y compensa el nuevo cuando la transacción hace rollback, incluidos fallos posteriores de auditoría. La limpieza fallida se registra sin convertir un commit exitoso en error; no hay una cola persistente de reintentos de Cloudinary.

El token inicial se introduce manualmente y no se guarda en frontend. Logos Cloudinary limitados a 2 MB, PNG/JPEG y dimensiones verificadas; namespace distingue instalaciones. El perfil se utiliza en presentación y tiquetes; esta fase no implementa facturación electrónica DIAN.

### Condición de responsable de IVA

La entidad Empresa incorpora `responsableIva`, persistido en la columna nullable `responsable_iva` de tipo boolean. `true` significa responsable, `false` no responsable y `null` pendiente de configurar. No se deduce del tamaño del negocio ni se asigna un valor por defecto a instalaciones existentes.

POST y PUT aceptan el campo adicional `responsableIva` como booleano JSON. Durante la transición del frontend es opcional: omitirlo o enviar `null` conserva el dato existente al actualizar y deja pendiente la condición al crear. Enviar `false` sí actualiza el dato; una vez configurado, `null` no permite borrarlo. Por tanto, el PUT admite los nueve campos anteriores más este dato opcional. No hay endpoint fiscal adicional.

GET y las respuestas de registro, actualización y logo incluyen el campo. La lectura sigue siendo pública para personalización; la escritura conserva ACTUALIZACION EMPRESA y el bloqueo de fila. La auditoría incluye el nombre `responsableIva` cuando cambia, sin copiar el payload. No se modifican productos, costos, cálculos de ventas, reportes ni comprobantes en esta etapa.

Ejemplo de propiedad que se añade al cuerpo completo de registro/actualización:

```json
{ "responsableIva": true }
```

Este fragmento no es un PUT completo: los demás campos obligatorios siguen siendo necesarios. El siguiente paso del frontend deberá solicitar una selección explícita. Antes de usar esta condición para liquidar impuestos, resolver los registros pendientes y guardar la condición aplicada en cada venta; nunca interpretar `null` como `false`.

Para bases existentes se incluye [sql/empresa-responsable-iva.sql](sql/empresa-responsable-iva.sql), aditivo e idempotente, sin asignar condiciones tributarias a datos previos. Revisarlo y aplicarlo explícitamente; su presencia no acredita que ya se ejecutó. Hibernate puede crear la columna al arrancar con `ddl-auto=update`; el script no se ejecuta automáticamente desde esta carpeta.

Validación de esta ampliación: 45 pruebas seleccionadas sin fallos (EmpresaServiceTest, EmpresaHttpTest, EmpresaRequestValidationTest, CloudinaryEmpresaLogoStorageTest y AutorizacionInterceptorTest). Cubren valores true/false, condición pendiente, conservación ante solicitudes antiguas, auditoría, permisos y conservación al cambiar logo. Son pruebas unitarias y MVC con servicios/identidad simulados; no validan persistencia o migración en PostgreSQL real ni JWT real.

## Errores y rendimiento

Errores JSON con status, error, message y path; validación agrega field/fieldErrors según el caso. 400 para entrada inválida, 401 para autenticación, 403 para autorización, 404 para inexistentes y 409 para conflictos. No exponer SQL o secretos.

Aplicar filtros y orden en servidor, evitar N+1 y seleccionar campos necesarios. No asegurar rendimiento por usar paginación: medir consultas reales y COUNT con EXPLAIN (ANALYZE, BUFFERS) antes de incorporar índices. Búsqueda parcial de usuarios y búsqueda por prefijo de otros dominios son contratos distintos.

## Pruebas y estado de validación

Maven muestra deprecaciones y advertencias de tipos genéricos (`unchecked` y `rawtypes`) mediante la configuración del compilador. Corregir su causa sin ocultarlas globalmente. Estos diagnósticos de javac no sustituyen los del panel de Problemas de VS Code, que puede incluir análisis de nulabilidad y avisos de extensiones.

Tras cambiar pom.xml, actualizar la configuración Java del proyecto en VS Code para renovar el classpath y los diagnósticos del editor. La actualización de Maven por consola no confirma que el servidor de lenguaje ya haya importado esos cambios. Los órdenes estáticos de módulos y auditoría usan TypedPropertyPath; los campos recibidos por API siguen siendo textuales y se validan en cada dominio.

Para comprobar compilación limpia y pruebas sin iniciar Spring contra servicios reales: `.\mvnw.cmd "-Dtest=*Test,!UsuarioRepositoryPostgresTest" clean test`. El patrón excluye tanto la prueba PostgreSQL opcional como `SistemaposjcsApplicationTests`; no acredita el arranque integral del contexto. Los logs de fallos simulados y el aviso de la JVM sobre CDS durante instrumentación Mockito no equivalen a advertencias de compilación.

El refactor de perfil de empresa del 6 de noviembre verificó 40 pruebas seleccionadas de empresa, autorización y autenticación sin fallos. Incluyen MockMvc con identidad/servicios simulados, validación de imágenes reales en memoria con Cloudinary simulado y callbacks de commit/rollback simulados. No prueban los bloqueos en PostgreSQL real ni un flujo HTTP con JWT y Cloudinary reales. Ejecución reproducible:

```powershell
.\mvnw.cmd "-Dtest=EmpresaServiceTest,EmpresaHttpTest,EmpresaRequestValidationTest,CloudinaryEmpresaLogoStorageTest,AutorizacionInterceptorTest,AuthServiceTest,AuthControllerTest,JwtTokenRevocationServiceTest" test
```

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean verify
git diff --check
```

La prueba opcional UsuarioRepositoryPostgresTest se activa con POS_TEST_DB_URL, POS_TEST_DB_USER y POS_TEST_DB_PASSWORD. Crea y elimina un esquema aislado con 520 usuarios sintéticos; requiere una base de prueba con permisos para crear esquemas. No colocar secretos en comandos publicados o documentación.

La fase de usuarios registró el 3 de noviembre 57 pruebas seleccionadas sin fallos, incluida esa prueba real de PostgreSQL. Las pruebas MockMvc y de servicios usan dobles; las del navegador usaron una API simulada. No afirmar validación integral de JWT/servicios reales ni garantía de que una base nueva esté aprovisionada.

Leer [AGENTS](AGENTS.md) y la [skill técnica](.cursorrules/backend-pos-architect/SKILL.md). La separación futura de repositorios no se ejecuta por estas instrucciones.
