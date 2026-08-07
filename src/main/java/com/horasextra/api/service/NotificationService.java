package com.horasextra.api.service;

import com.horasextra.api.entity.Coordinator;
import com.horasextra.api.entity.OvertimeRequest;
import com.horasextra.api.entity.OvertimeRequestStatus;
import com.horasextra.api.repository.OvertimeRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final MailService mailService;

    public NotificationService(OvertimeRequestRepository overtimeRequestRepository, MailService mailService) {
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.mailService = mailService;
    }

    @Transactional
    public void notifyCoordinatorsWithPendingRequests() {
        List<OvertimeRequest> pending = overtimeRequestRepository
                .findByStatusAndCoordinatorNotifiedAtIsNull(OvertimeRequestStatus.PENDING_COORDINATOR);

        Map<Coordinator, List<OvertimeRequest>> byCoordinator = pending.stream()
                .collect(Collectors.groupingBy(OvertimeRequest::getCoordinator));

        byCoordinator.forEach((coordinator, requests) -> {
            try {
                mailService.send(coordinator.getEmail(),
                        "Horas Extra - Solicitudes pendientes de tu aprobacion",
                        buildCoordinatorEmailBody(coordinator, requests));

                LocalDateTime now = LocalDateTime.now();
                requests.forEach(request -> request.setCoordinatorNotifiedAt(now));
                overtimeRequestRepository.saveAll(requests);
            } catch (Exception ex) {
                log.warn("No se pudo enviar el correo semanal al coordinador {} ({}): {}",
                        coordinator.getFullName(), coordinator.getEmail(), ex.getMessage());
            }
        });
    }

    private String buildCoordinatorEmailBody(Coordinator coordinator, List<OvertimeRequest> requests) {
        StringBuilder html = new StringBuilder();
        html.append("<p>Hola ").append(coordinator.getFullName()).append(",</p>")
                .append("<p>Tienes las siguientes solicitudes de horas extra pendientes de tu aprobacion:</p>")
                .append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">")
                .append("<tr><th>Empleado</th><th>Periodo</th><th>Horas</th><th>Justificacion</th></tr>");

        for (OvertimeRequest request : requests) {
            html.append("<tr>")
                    .append("<td>").append(request.getEmployee().getFullName()).append("</td>")
                    .append("<td>").append(request.getPeriodYear()).append("-")
                    .append(request.getPeriodMonth()).append(" (")
                    .append(request.getPeriodFortnight()).append(")</td>")
                    .append("<td>").append(request.getHoursRequested()).append("</td>")
                    .append("<td>").append(request.getJustification() == null ? "" : request.getJustification())
                    .append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }
}
