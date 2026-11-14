package com.gestaofinanceira.renda.dto;

import java.math.BigDecimal;

import com.gestaofinanceira.renda.entity.Periodicidade;
import com.gestaofinanceira.renda.entity.TipoRenda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RendaRequest(

        @NotBlank(message = "O nome da renda é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotNull(message = "O tipo é obrigatório (SALARIO, RECORRENTE ou VARIAVEL)")
        TipoRenda tipo,

        @DecimalMin(value = "0.00", message = "O valor de referência não pode ser negativo")
        @Digits(integer = 8, fraction = 2,
                message = "O valor deve ter no máximo 8 inteiros e 2 decimais")
        BigDecimal valorReferencia,

        Boolean recorrente,

        Periodicidade periodicidade
) {
}
