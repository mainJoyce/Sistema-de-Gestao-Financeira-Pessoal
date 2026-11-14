package com.gestaofinanceira.recomendacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecomendacaoInvestimentoResponse(
        Long id,
        String tipo,
        String titulo,
        String descricao,
        BigDecimal valorSugerido,
        String justificativa,
        String versaoRegra,
        LocalDateTime dataCriacao
) {
}