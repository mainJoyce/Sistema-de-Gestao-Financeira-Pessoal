package com.gestaofinanceira.dashboard.dto;

import java.math.BigDecimal;

public record EstabelecimentoGastoResponse(
        String estabelecimento,
        BigDecimal valor,
        BigDecimal percentual
) {
}
