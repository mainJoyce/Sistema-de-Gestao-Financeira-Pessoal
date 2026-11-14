package com.gestaofinanceira.orcamento.service;

import java.math.BigDecimal;

/**
 * Faixas do documento (secao 3.4): ate 70% Normal | 71 a 89% Atencao |
 * 90% ou mais Risco | acima de 100% Estourado.
 * O documento deixa uma lacuna entre 89% e 90%: aqui, acima de 70% e abaixo de 90% e Atencao.
 */
public final class FaixaOrcamento {

    private static final BigDecimal LIMITE_ESTOURADO = new BigDecimal("100");
    private static final BigDecimal LIMITE_RISCO = new BigDecimal("90");
    private static final BigDecimal LIMITE_ATENCAO = new BigDecimal("70");

    private FaixaOrcamento() {
    }

    public static String classificar(BigDecimal percentualUsado) {
        if (percentualUsado.compareTo(LIMITE_ESTOURADO) > 0) {
            return "ESTOURADO";
        }
        if (percentualUsado.compareTo(LIMITE_RISCO) >= 0) {
            return "RISCO";
        }
        if (percentualUsado.compareTo(LIMITE_ATENCAO) > 0) {
            return "ATENCAO";
        }
        return "NORMAL";
    }
}
