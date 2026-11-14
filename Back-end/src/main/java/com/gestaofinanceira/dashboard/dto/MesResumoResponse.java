package com.gestaofinanceira.dashboard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MesResumoResponse(
        YearMonth mes,
        BigDecimal receitaTotal,
        BigDecimal despesaTotal,
        BigDecimal saldo,
        BigDecimal economiaAcumulada
) {
}
