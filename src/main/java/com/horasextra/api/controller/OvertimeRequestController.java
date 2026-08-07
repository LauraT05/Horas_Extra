package com.horasextra.api.controller;

import com.horasextra.api.dto.CreateOvertimeRequestDto;
import com.horasextra.api.dto.OvertimeRequestResponseDto;
import com.horasextra.api.entity.OvertimeRequest;
import com.horasextra.api.mapper.OvertimeRequestMapper;
import com.horasextra.api.service.OvertimeRequestService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;

    public OvertimeRequestController(OvertimeRequestService overtimeRequestService) {
        this.overtimeRequestService = overtimeRequestService;
    }

    @PostMapping("/api/v1/overtime-requests")
    public ResponseEntity<OvertimeRequestResponseDto> create(@Valid @RequestBody CreateOvertimeRequestDto dto) {
        OvertimeRequest created = overtimeRequestService.create(dto);
        OvertimeRequestResponseDto response = OvertimeRequestMapper.toResponseDto(created);
        return ResponseEntity.created(URI.create("/api/v1/overtime-requests/" + created.getId())).body(response);
    }

    @GetMapping("/api/v1/overtime-requests")
    public List<OvertimeRequestResponseDto> findByEmployee(@RequestParam Long employeeId) {
        return overtimeRequestService.findByEmployee(employeeId).stream()
                .map(OvertimeRequestMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/api/v1/overtime-requests/{id}")
    public OvertimeRequestResponseDto findById(@PathVariable Long id) {
        return OvertimeRequestMapper.toResponseDto(overtimeRequestService.findById(id));
    }
}
