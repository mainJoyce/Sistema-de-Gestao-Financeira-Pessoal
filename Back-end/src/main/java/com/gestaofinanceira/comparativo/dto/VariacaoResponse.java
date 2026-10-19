package com.gestaofinanceira.comparativo.dto;

import java.math.BigDecimal;

public record VariacaoResponse(BigDecimal valor, BigDecimal percentual) {
}