package com.gestaofinanceira.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Variacoes {

    private Variacoes() {
    }

    // (atual - base) / |base| * 100. Devolve null se a base for zero.
    public static BigDecimal percentual(BigDecimal atual, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return atual.subtract(base)
                .multiply(BigDecimal.valueOf(100))
                .divide(base.abs(), 2, RoundingMode.HALF_UP);
    }
}