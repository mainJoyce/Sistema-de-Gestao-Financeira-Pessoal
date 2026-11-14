package com.gestaofinanceira.recomendacao.dto;

import java.math.BigDecimal;

/**
 * situacao: DENTRO_DO_IDEAL ou ACIMA_DO_IDEAL (necessidades e desejos);
 * META_ATINGIDA ou ABAIXO_DO_IDEAL (investimentos).
 */
public record FaixaPlanoResponse(
        String grupo,
        BigDecimal percentualIdeal,
        BigDecimal valorIdeal,
        BigDecimal valorGasto,
        BigDecimal percentualDaRenda,
        BigDecimal diferenca,
        String situacao
) {
}
