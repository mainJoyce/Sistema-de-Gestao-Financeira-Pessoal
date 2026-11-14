package com.gestaofinanceira.meta.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record AporteRequest(

        @NotNull(message = "O valor do aporte é obrigatório")
        @DecimalMin(value = "0.01", message = "O aporte deve ser maior que zero")
        @Digits(integer = 8, fraction = 2,
                message = "O valor deve ter no máximo 8 inteiros e 2 decimais")
        BigDecimal valor
) {
}
