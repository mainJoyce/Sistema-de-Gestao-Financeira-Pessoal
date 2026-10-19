package com.gestaofinanceira.orcamento.dto;

import java.math.BigDecimal;

public record OrcamentoResponse(
    Long id,
    Long categoriaId,
    BigDecimal valorLimite,
    BigDecimal valorGasto,
    BigDecimal saldoRestante,
    BigDecimal percentualUsado,
    String classificacao,
    Integer mes,
    Integer ano
) {}
