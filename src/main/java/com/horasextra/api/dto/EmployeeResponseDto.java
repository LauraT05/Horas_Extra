package com.horasextra.api.dto;

public record EmployeeResponseDto(
        Long id,
        String fullName,
        String email,
        boolean active
) {
}
