package com.gestaofinanceira.insight.dto;

import java.time.LocalDateTime;

public record InsightResponse(
        Long id,
        String titulo,
        String descricao,
        String tipo,
        String justificativa,
        String dadosReferencia,
        LocalDateTime dataCriacao
) {
}