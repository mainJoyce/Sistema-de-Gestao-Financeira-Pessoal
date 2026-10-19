package com.gestaofinanceira.dashboard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record DashboardResponse(
        YearMonth mes,
        BigDecimal receitaTotal,
        BigDecimal despesaTotal,
        BigDecimal saldo,
        BigDecimal percentualComprometimento,
        List<GastoCategoriaResponse> gastosPorCategoria
) {
}