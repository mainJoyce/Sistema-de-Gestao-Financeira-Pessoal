package com.gestaofinanceira.meta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MetaRequest(

    @NotBlank(message = "O nome da meta é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    String nome,

    @NotNull(message = "O valor objetivo é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor objetivo deve ser maior que zero")
    BigDecimal valorObjetivo,

    @NotNull(message = "A data desejada é obrigatória")
    LocalDate dataDesejada

) {}