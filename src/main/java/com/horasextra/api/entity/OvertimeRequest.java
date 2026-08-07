package com.horasextra.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "overtime_request")
@Getter
@Setter
@NoArgsConstructor
public class OvertimeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coordinator_id", nullable = false)
    private Coordinator coordinator;

    // Solo se asigna cuando el coordinador aprueba y elige a quien escala la solicitud.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;

    @Column(nullable = false)
    private Integer periodYear;

    @Column(nullable = false)
    private Integer periodMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Fortnight periodFortnight;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hoursRequested;

    @Column(length = 1000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OvertimeRequestStatus status = OvertimeRequestStatus.PENDING_COORDINATOR;

    @Column(length = 1000)
    private String coordinatorComment;

    private LocalDateTime coordinatorDecisionAt;

    @Column(length = 1000)
    private String directorComment;

    private LocalDateTime directorDecisionAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Cada campo marca cuando ya se incluyo la solicitud en el correo semanal
    // correspondiente; se deja null hasta que el envio a ese destinatario sea exitoso,
    // para no reenviar en la siguiente corrida del job.
    private LocalDateTime coordinatorNotifiedAt;

    private LocalDateTime directorNotifiedAt;

    private LocalDateTime employeeRejectionNotifiedAt;

    private LocalDateTime payrollNotifiedAt;
}
