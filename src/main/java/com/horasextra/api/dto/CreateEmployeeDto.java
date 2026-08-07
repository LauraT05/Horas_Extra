package com.horasextra.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateEmployeeDto(
        @NotBlank String fullName,
        @NotBlank @Email String email
) {
}
