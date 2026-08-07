package com.horasextra.api.dto;

import com.horasextra.api.entity.Fortnight;
import com.horasextra.api.entity.OvertimeRequestStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OvertimeRequestResponseDto(
        Long id,
        Long employeeId,
        String employeeFullName,
        Long coordinatorId,
        String coordinatorFullName,
        Long directorId,
        String directorFullName,
        Integer periodYear,
        Integer periodMonth,
        Fortnight periodFortnight,
        BigDecimal hoursRequested,
        String justification,
        OvertimeRequestStatus status,
        LocalDateTime createdAt
) {
}
