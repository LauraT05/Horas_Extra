package com.horasextra.api.mapper;

import com.horasextra.api.dto.EmployeeResponseDto;
import com.horasextra.api.entity.Employee;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static EmployeeResponseDto toResponseDto(Employee employee) {
        return new EmployeeResponseDto(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.isActive()
        );
    }
}
