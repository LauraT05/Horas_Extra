package com.horasextra.api.dto;

import com.horasextra.api.entity.Fortnight;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateOvertimeRequestDto(
        @NotNull Long employeeId,
        @NotNull Long coordinatorId,
        @NotNull @Min(2000) @Max(2100) Integer periodYear,
        @NotNull @Min(1) @Max(12) Integer periodMonth,
        @NotNull Fortnight periodFortnight,
        @NotNull @DecimalMin(value = "0.01") BigDecimal hoursRequested,
        @Size(max = 1000) String justification
) {
}
