package com.gestaofinanceira.dashboard.dto;

import java.math.BigDecimal;

public record GastoCategoriaResponse(
        Long categoriaId,
        String categoria,
        BigDecimal valor,
        BigDecimal percentual
) {
}