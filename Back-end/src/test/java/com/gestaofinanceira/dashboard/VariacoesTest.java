package com.gestaofinanceira.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.gestaofinanceira.dashboard.service.Variacoes;

class VariacoesTest {

    @Test
    void calculaAumentoPercentual() {
        assertEquals(new BigDecimal("50.00"),
                Variacoes.percentual(new BigDecimal("300"), new BigDecimal("200")));
    }

    @Test
    void calculaReducaoPercentual() {
        assertEquals(new BigDecimal("-25.00"),
                Variacoes.percentual(new BigDecimal("150"), new BigDecimal("200")));
    }

    @Test
    void baseZeroNaoTemPercentual() {
        assertNull(Variacoes.percentual(new BigDecimal("100"), BigDecimal.ZERO));
    }
}
