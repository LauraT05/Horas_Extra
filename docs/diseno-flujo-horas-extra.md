# Diseño — API de Aprobación de Horas Extra

> Documento de referencia del diseño acordado. Se implementa de forma incremental; cada incremento se revisa y aprueba antes de seguir con el siguiente.

## Estado actual (2026-08-07)

**Hecho y en `master`:**
- Setup completo del proyecto (Maven, H2, Mail, scheduler).
- Incremento 1: crear solicitud de horas extra (`POST /api/v1/overtime-requests`) + notificación semanal consolidada al coordinador, con trigger manual de prueba. Verificado end-to-end (idempotencia, agrupación por coordinador, manejo de errores).

**Hecho, en rama `feature/empleados-crud` (pendiente de tu revisión, sin mergear a `master`):**
- `GET /api/v1/employees` y `POST /api/v1/employees` (no estaban en el diseño original, se agregaron a solicitud).
- `Employee` ahora incluye `documentType` (enum `CC`/`CE`/`TI`/`PA`) y `documentNumber` (único), además de `fullName`/`email`.
- Manejo de conflicto 409 si el email o el número de documento ya existen.

**Pendiente (no iniciado):**
- Incrementos 2 a 6 (ver hoja de ruta abajo) — decisión del coordinador, notificación al director, decisión del director, notificación a nómina, notificación de rechazo al empleado.
- Endpoints equivalentes de consulta/creación para `Coordinator` y `Director` (por ahora solo hay catálogo de coordinadores vía `GET /api/v1/coordinators`, sin `POST`).

## Flujo de negocio

1. Un **empleado** ingresa las horas extra de una quincena y elige a qué **coordinador** las envía.
2. **Semanalmente**, cada coordinador recibe un correo consolidado con sus solicitudes pendientes (no hay aviso inmediato evento a evento).
3. El coordinador aprueba (eligiendo a qué **director** pasa la solicitud) o rechaza.
4. Semanalmente, cada director recibe su correo consolidado de pendientes.
5. El director aprueba o rechaza.
6. Si algo fue rechazado (por coordinador o director), el empleado recibe correo semanal de seguimiento. Si el coordinador rechaza, no se asigna ningún director.
7. Si una solicitud pasó ambos filtros, **nómina** recibe semanalmente el consolidado de solicitudes aprobadas para llevarlas a su sistema contable.

## Decisiones acordadas

- **Base de datos**: H2 en memoria para esta fase de desarrollo. Migrar a Postgres/MySQL más adelante es solo cambio de configuración.
- **Sin autenticación todavía**: el actor (empleado/coordinador/director) se identifica por id explícito en la petición. Seguridad real (Spring Security) queda como funcionalidad futura.
- **Notificaciones**: todas semanales y consolidadas por destinatario (coordinador, director, empleado en caso de rechazo, nómina). Nada es inmediato.

## Modelo de dominio

Paquete `com.horasextra.api.entity`:

- **Employee** (id, fullName, email, documentType [enum `CC`/`CE`/`TI`/`PA`], documentNumber [único], active) — CRUD de consulta/creación ya implementado en `feature/empleados-crud`
- **Coordinator** (id, fullName, email, active)
- **Director** (id, fullName, email, active)
- **PayrollRecipient** (id, fullName, email, active) — destinatarios del correo de nómina
- **OvertimeRequest**: id, `employee` (M2O), `coordinator` (M2O), `director` (M2O, **nullable**, se asigna solo al aprobar el coordinador), `periodYear`, `periodMonth`, `periodFortnight` (enum `Fortnight{FIRST,SECOND}`), `hoursRequested` (BigDecimal), `justification`, `status` (enum `OvertimeRequestStatus`), `coordinatorComment`, `coordinatorDecisionAt`, `directorComment`, `directorDecisionAt`, `createdAt`, `updatedAt`, y 4 timestamps de notificación nullable: `coordinatorNotifiedAt`, `directorNotifiedAt`, `employeeRejectionNotifiedAt`, `payrollNotifiedAt`.

**OvertimeRequestStatus**: `PENDING_COORDINATOR`, `REJECTED_COORDINATOR`, `PENDING_DIRECTOR`, `REJECTED_DIRECTOR`, `APPROVED`.

**Por qué 4 timestamps de notificación y no un estado "notificada"**: estado-del-flujo y ya-se-avisó-por-correo son dimensiones ortogonales — una misma solicitud puede necesitar notificar a más de un rol a lo largo de su vida. El job semanal filtra por `status = X AND xNotifiedAt IS NULL`, envía, y solo marca el timestamp si el envío fue exitoso. Así los reintentos no duplican correos y un fallo puntual de un destinatario no bloquea a los demás.

## Estructura de paquetes (`com.horasextra.api`)

```
config/SchedulingConfig.java
controller/ (OvertimeRequestController, CoordinatorController, EmployeeController, DirectorController*, PayrollController*, admin/SchedulerAdminController)
dto/        (Create/Response DTOs, Approve/Reject DTOs por rol)
entity/     (Employee, Coordinator, Director, PayrollRecipient*, OvertimeRequest, OvertimeRequestStatus, Fortnight, DocumentType)
repository/ (uno por entidad, JpaRepository + queries derivadas)
service/    (OvertimeRequestService, NotificationService, MailService)
scheduler/  (WeeklyNotificationScheduler)
mapper/     (OvertimeRequestMapper, EmployeeMapper — métodos estáticos, sin MapStruct)
exception/  (ResourceNotFoundException, InvalidRequestStateException*, GlobalExceptionHandler)
```
`*` = todavía no creado en código, planeado para un incremento futuro.

Servicios como clases concretas (sin interfaz+impl) y mapper manual: se prioriza simplicidad en esta etapa.

## Endpoints (diseño completo del flujo)

| Método | Path | Descripción | Estado |
|---|---|---|---|
| GET | `/api/v1/employees` | listar empleados | ✅ `feature/empleados-crud` |
| POST | `/api/v1/employees` | crear empleado (fullName, email, documentType, documentNumber) | ✅ `feature/empleados-crud` |
| GET | `/api/v1/coordinators` | catálogo para elegir coordinador | ✅ `master` |
| GET | `/api/v1/directors` | catálogo para elegir director | ⬜ pendiente (Incremento 2) |
| POST | `/api/v1/overtime-requests` | crear solicitud → `PENDING_COORDINATOR` | ✅ `master` |
| GET | `/api/v1/overtime-requests?employeeId=` | solicitudes propias | ✅ `master` |
| GET | `/api/v1/overtime-requests/{id}` | detalle | ✅ `master` |
| GET | `/api/v1/coordinators/{id}/overtime-requests?status=` | bandeja coordinador | ⬜ pendiente (Incremento 2) |
| POST | `/api/v1/coordinators/{id}/overtime-requests/{id}/approve` | aprueba + elige director → `PENDING_DIRECTOR` | ⬜ pendiente (Incremento 2) |
| POST | `/api/v1/coordinators/{id}/overtime-requests/{id}/reject` | → `REJECTED_COORDINATOR` | ⬜ pendiente (Incremento 2) |
| GET | `/api/v1/directors/{id}/overtime-requests?status=` | bandeja director | ⬜ pendiente (Incremento 4) |
| POST | `/api/v1/directors/{id}/overtime-requests/{id}/approve` | → `APPROVED` | ⬜ pendiente (Incremento 4) |
| POST | `/api/v1/directors/{id}/overtime-requests/{id}/reject` | → `REJECTED_DIRECTOR` | ⬜ pendiente (Incremento 4) |
| GET | `/api/v1/payroll/overtime-requests?status=APPROVED` | consulta nómina | ⬜ pendiente (Incremento 5) |
| POST | `/api/v1/admin/scheduler/notify-coordinators` | disparo manual de la rutina semanal a coordinadores | ✅ `master` |
| POST | `/api/v1/admin/scheduler/notify-directors` | ídem para directores | ⬜ pendiente (Incremento 3) |
| POST | `/api/v1/admin/scheduler/notify-employee-rejections` | ídem para rechazos a empleados | ⬜ pendiente (Incremento 6) |
| POST | `/api/v1/admin/scheduler/notify-payroll` | ídem para nómina | ⬜ pendiente (Incremento 5) |

Los endpoints de aprobar/rechazar validan el estado actual y devolverán 409 (`InvalidRequestStateException`, aún no implementada) si no corresponde.

## Job semanal

`WeeklyNotificationScheduler` (`@Scheduled(cron = "${horasextra.scheduler.weekly-cron:0 0 7 * * MON}")`) llama, en orden, a 4 métodos de `NotificationService`:

1. `notifyCoordinatorsWithPendingRequests()`
2. `notifyDirectorsWithPendingRequests()`
3. `notifyEmployeesOfRejections()`
4. `notifyPayrollOfApprovedRequests()`

Cada uno: consulta por `status` + `xNotifiedAt IS NULL` → agrupa por destinatario → arma un correo HTML (tabla simple) → envía vía `MailService` (JavaMailSender) → marca el timestamp solo si el envío no lanzó excepción (try/catch por destinatario).

Cron y remitente configurables en `application.properties`. En desarrollo, el SMTP apunta a algo tipo MailHog/log.

## Hoja de ruta por incrementos

- ✅ **Incremento 1** (en `master`): setup Maven completo, modelo de datos completo, catálogo de coordinadores, crear/listar solicitud, `notifyCoordinatorsWithPendingRequests()` + trigger manual de prueba.
- ✅ **Extra, no planeado originalmente** (en `feature/empleados-crud`, pendiente de revisión/merge): `GET`/`POST /api/v1/employees`, con `documentType`/`documentNumber`.
- ⬜ **Incremento 2** (siguiente): aprobar (con selección de director) / rechazar como coordinador. Requiere `DirectorController` (catálogo), `DirectorRepository`, `CoordinatorApproveDto`/`CoordinatorRejectDto`, y `InvalidRequestStateException` para el 409 cuando el estado no corresponde.
- ⬜ **Incremento 3**: notificación semanal al director.
- ⬜ **Incremento 4**: aprobar/rechazar como director.
- ⬜ **Incremento 5**: notificación semanal a nómina + endpoint de consulta. Requiere activar la entidad `PayrollRecipient` (ya diseñada, aún no creada en código).
- ⬜ **Incremento 6**: notificación semanal de rechazo al empleado.
- **Futuro** (no solicitado aún): paginación, `ProblemDetail` para errores, tests de integración, validar duplicados en `OvertimeRequest`, Spring Security, migración a Postgres/MySQL, endpoints de consulta/creación para `Coordinator`/`Director`.
