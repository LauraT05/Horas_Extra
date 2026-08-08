package com.horasextra.api.dto;

import com.horasextra.api.entity.DocumentType;

public record EmployeeResponseDto(
        Long id,
        String fullName,
        String email,
        DocumentType documentType,
        String documentNumber,
        boolean active
) {
}
