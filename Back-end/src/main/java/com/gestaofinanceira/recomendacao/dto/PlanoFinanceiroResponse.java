package com.gestaofinanceira.recomendacao.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record PlanoFinanceiroResponse(
        YearMonth mes,
        BigDecimal rendaBase,
        FaixaPlanoResponse necessidades,
        FaixaPlanoResponse desejos,
        FaixaPlanoResponse investimentos
) {
}
