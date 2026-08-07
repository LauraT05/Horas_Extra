package com.horasextra.api.controller.admin;

import com.horasextra.api.service.NotificationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints solo para disparar manualmente las rutinas del job semanal durante pruebas
// (esperar una semana real no es practico en desarrollo). No debe existir en produccion
// hasta que haya autenticacion/autorizacion real delante.
@RestController
@Profile("!prod")
public class SchedulerAdminController {

    private final NotificationService notificationService;

    public SchedulerAdminController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/api/v1/admin/scheduler/notify-coordinators")
    public ResponseEntity<Void> notifyCoordinators() {
        notificationService.notifyCoordinatorsWithPendingRequests();
        return ResponseEntity.ok().build();
    }
}
