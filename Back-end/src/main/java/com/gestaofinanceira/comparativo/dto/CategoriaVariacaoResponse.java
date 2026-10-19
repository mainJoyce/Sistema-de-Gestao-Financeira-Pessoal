package com.gestaofinanceira.comparativo.dto;

import java.math.BigDecimal;

public record CategoriaVariacaoResponse(
        Long categoriaId,
        String categoria,
        BigDecimal valorAtual,
        BigDecimal valorAnterior,
        VariacaoResponse variacao
) {
}