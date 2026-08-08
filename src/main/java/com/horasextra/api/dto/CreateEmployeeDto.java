package com.horasextra.api.dto;

import com.horasextra.api.entity.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmployeeDto(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotNull DocumentType documentType,
        @NotBlank String documentNumber
) {
}
