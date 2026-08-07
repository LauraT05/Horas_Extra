package com.horasextra.api.controller;

import com.horasextra.api.dto.CoordinatorSummaryDto;
import com.horasextra.api.mapper.OvertimeRequestMapper;
import com.horasextra.api.repository.CoordinatorRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoordinatorController {

    private final CoordinatorRepository coordinatorRepository;

    public CoordinatorController(CoordinatorRepository coordinatorRepository) {
        this.coordinatorRepository = coordinatorRepository;
    }

    @GetMapping("/api/v1/coordinators")
    public List<CoordinatorSummaryDto> findActiveCoordinators() {
        return coordinatorRepository.findByActiveTrue().stream()
                .map(OvertimeRequestMapper::toSummaryDto)
                .toList();
    }
}
