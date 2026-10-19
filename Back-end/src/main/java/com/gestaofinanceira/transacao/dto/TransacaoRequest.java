package com.gestaofinanceira.transacao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gestaofinanceira.transacao.entity.Recorrencia;
import com.gestaofinanceira.transacao.entity.TipoTransacao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransacaoRequest(

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.00", inclusive = false, message = "O valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Valor inválido (máximo 8 inteiros e 2 decimais)")
        BigDecimal valor,

        @NotNull(message = "A data é obrigatória")
        LocalDate data,

        @NotNull(message = "O tipo é obrigatório (RECEITA ou DESPESA)")
        TipoTransacao tipo,

        @Size(max = 150, message = "O estabelecimento deve ter no máximo 150 caracteres")
        String estabelecimento,

        Recorrencia recorrencia,

        Long categoriaId
) {
}