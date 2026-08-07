package com.horasextra.api.mapper;

import com.horasextra.api.dto.CoordinatorSummaryDto;
import com.horasextra.api.dto.OvertimeRequestResponseDto;
import com.horasextra.api.entity.Coordinator;
import com.horasextra.api.entity.Director;
import com.horasextra.api.entity.OvertimeRequest;

public final class OvertimeRequestMapper {

    private OvertimeRequestMapper() {
    }

    public static OvertimeRequestResponseDto toResponseDto(OvertimeRequest request) {
        Director director = request.getDirector();
        return new OvertimeRequestResponseDto(
                request.getId(),
                request.getEmployee().getId(),
                request.getEmployee().getFullName(),
                request.getCoordinator().getId(),
                request.getCoordinator().getFullName(),
                director != null ? director.getId() : null,
                director != null ? director.getFullName() : null,
                request.getPeriodYear(),
                request.getPeriodMonth(),
                request.getPeriodFortnight(),
                request.getHoursRequested(),
                request.getJustification(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    public static CoordinatorSummaryDto toSummaryDto(Coordinator coordinator) {
        return new CoordinatorSummaryDto(
                coordinator.getId(),
                coordinator.getFullName(),
                coordinator.getEmail()
        );
    }
}
