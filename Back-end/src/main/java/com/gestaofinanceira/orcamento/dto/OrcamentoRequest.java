package com.gestaofinanceira.orcamento.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrcamentoRequest(

    @NotNull(message = "A categoria é obrigatória")
    @Positive(message = "A categoria deve ser válida")
    Long categoriaId,

    @NotNull(message = "O valor limite é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor limite deve ser maior que zero")
    BigDecimal valorLimite,

    @NotNull(message = "O mês é obrigatório")
    @Min(value = 1, message = "O mês deve ser entre 1 e 12")
    @Max(value = 12, message = "O mês deve ser entre 1 e 12")
    Integer mes,

    @NotNull(message = "O ano é obrigatório")
    @Min(value = 2000, message = "O ano deve ser válido")
    Integer ano

) {}