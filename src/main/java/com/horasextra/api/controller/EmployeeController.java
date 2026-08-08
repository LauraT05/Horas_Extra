package com.horasextra.api.controller;

import com.horasextra.api.dto.CreateEmployeeDto;
import com.horasextra.api.dto.EmployeeResponseDto;
import com.horasextra.api.entity.Employee;
import com.horasextra.api.mapper.EmployeeMapper;
import com.horasextra.api.repository.EmployeeRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/api/v1/employees")
    public List<EmployeeResponseDto> findAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeMapper::toResponseDto)
                .toList();
    }

    @PostMapping("/api/v1/employees")
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody CreateEmployeeDto dto) {
        Employee employee = new Employee();
        employee.setFullName(dto.fullName());
        employee.setEmail(dto.email());
        employee.setDocumentType(dto.documentType());
        employee.setDocumentNumber(dto.documentNumber());
        employee.setActive(true);

        Employee saved = employeeRepository.save(employee);
        EmployeeResponseDto response = EmployeeMapper.toResponseDto(saved);
        return ResponseEntity.created(URI.create("/api/v1/employees/" + saved.getId())).body(response);
    }
}
