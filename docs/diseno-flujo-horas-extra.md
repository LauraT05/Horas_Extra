# Diseño — API de Aprobación de Horas Extra

> Documento de referencia del diseño acordado. Se implementa de forma incremental; cada incremento se revisa y aprueba antes de seguir con el siguiente.

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

- **Employee** (id, fullName, email, active)
- **Coordinator** (id, fullName, email, active)
- **Director** (id, fullName, email, active)
- **PayrollRecipient** (id, fullName, email, active) — destinatarios del correo de nómina
- **OvertimeRequest**: id, `employee` (M2O), `coordinator` (M2O), `director` (M2O, **nullable**, se asigna solo al aprobar el coordinador), `periodYear`, `periodMonth`, `periodFortnight` (enum `Fortnight{FIRST,SECOND}`), `hoursRequested` (BigDecimal), `justification`, `status` (enum `OvertimeRequestStatus`), `coordinatorComment`, `coordinatorDecisionAt`, `directorComment`, `directorDecisionAt`, `createdAt`, `updatedAt`, y 4 timestamps de notificación nullable: `coordinatorNotifiedAt`, `directorNotifiedAt`, `employeeRejectionNotifiedAt`, `payrollNotifiedAt`.

**OvertimeRequestStatus**: `PENDING_COORDINATOR`, `REJECTED_COORDINATOR`, `PENDING_DIRECTOR`, `REJECTED_DIRECTOR`, `APPROVED`.

**Por qué 4 timestamps de notificación y no un estado "notificada"**: estado-del-flujo y ya-se-avisó-por-correo son dimensiones ortogonales — una misma solicitud puede necesitar notificar a más de un rol a lo largo de su vida. El job semanal filtra por `status = X AND xNotifiedAt IS NULL`, envía, y solo marca el timestamp si el envío fue exitoso. Así los reintentos no duplican correos y un fallo puntual de un destinatario no bloquea a los demás.

## Estructura de paquetes (`com.horasextra.api`)

```
config/SchedulingConfig.java
controller/ (OvertimeRequestController, CoordinatorController, DirectorController, PayrollController, admin/SchedulerAdminController)
dto/        (Create/Response DTOs, Approve/Reject DTOs por rol)
entity/     (Employee, Coordinator, Director, PayrollRecipient, OvertimeRequest, OvertimeRequestStatus, Fortnight)
repository/ (uno por entidad, JpaRepository + queries derivadas)
service/    (OvertimeRequestService, NotificationService, MailService)
scheduler/  (WeeklyNotificationScheduler)
mapper/     (OvertimeRequestMapper — métodos estáticos, sin MapStruct)
exception/  (ResourceNotFoundException, InvalidRequestStateException, GlobalExceptionHandler)
```

Servicios como clases concretas (sin interfaz+impl) y mapper manual: se prioriza simplicidad en esta etapa.

## Endpoints (diseño completo del flujo)

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/v1/coordinators` | catálogo para elegir coordinador |
| GET | `/api/v1/directors` | catálogo para elegir director |
| POST | `/api/v1/overtime-requests` | crear solicitud → `PENDING_COORDINATOR` |
| GET | `/api/v1/overtime-requests?employeeId=` | solicitudes propias |
| GET | `/api/v1/overtime-requests/{id}` | detalle |
| GET | `/api/v1/coordinators/{id}/overtime-requests?status=` | bandeja coordinador |
| POST | `/api/v1/coordinators/{id}/overtime-requests/{id}/approve` | aprueba + elige director → `PENDING_DIRECTOR` |
| POST | `/api/v1/coordinators/{id}/overtime-requests/{id}/reject` | → `REJECTED_COORDINATOR` |
| GET | `/api/v1/directors/{id}/overtime-requests?status=` | bandeja director |
| POST | `/api/v1/directors/{id}/overtime-requests/{id}/approve` | → `APPROVED` |
| POST | `/api/v1/directors/{id}/overtime-requests/{id}/reject` | → `REJECTED_DIRECTOR` |
| GET | `/api/v1/payroll/overtime-requests?status=APPROVED` | consulta nómina |
| POST | `/api/v1/admin/scheduler/notify-*` | disparo manual de cada rutina semanal (solo pruebas, `@Profile("!prod")`) |

Los endpoints de aprobar/rechazar validan el estado actual y devuelven 409 (`InvalidRequestStateException`) si no corresponde.

## Job semanal

`WeeklyNotificationScheduler` (`@Scheduled(cron = "${horasextra.scheduler.weekly-cron:0 0 7 * * MON}")`) llama, en orden, a 4 métodos de `NotificationService`:

1. `notifyCoordinatorsWithPendingRequests()`
2. `notifyDirectorsWithPendingRequests()`
3. `notifyEmployeesOfRejections()`
4. `notifyPayrollOfApprovedRequests()`

Cada uno: consulta por `status` + `xNotifiedAt IS NULL` → agrupa por destinatario → arma un correo HTML (tabla simple) → envía vía `MailService` (JavaMailSender) → marca el timestamp solo si el envío no lanzó excepción (try/catch por destinatario).

Cron y remitente configurables en `application.properties`. En desarrollo, el SMTP apunta a algo tipo MailHog/log.

## Hoja de ruta por incrementos

- **Incremento 1**: setup Maven completo, modelo de datos completo, catálogo de coordinadores, crear/listar solicitud, `notifyCoordinatorsWithPendingRequests()` + trigger manual de prueba.
- **Incremento 2**: aprobar (con selección de director) / rechazar como coordinador.
- **Incremento 3**: notificación semanal al director.
- **Incremento 4**: aprobar/rechazar como director.
- **Incremento 5**: notificación semanal a nómina + endpoint de consulta.
- **Incremento 6**: notificación semanal de rechazo al empleado.
- **Futuro** (no solicitado aún): paginación, `ProblemDetail` para errores, tests de integración, validar duplicados, Spring Security, migración a Postgres/MySQL.
