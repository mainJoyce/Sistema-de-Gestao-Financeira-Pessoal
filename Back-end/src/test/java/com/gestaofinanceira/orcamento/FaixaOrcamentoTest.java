package com.gestaofinanceira.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.gestaofinanceira.orcamento.service.FaixaOrcamento;

class FaixaOrcamentoTest {

    private String faixa(String percentual) {
        return FaixaOrcamento.classificar(new BigDecimal(percentual));
    }

    @Test
    void ate70eNormal() {
        assertEquals("NORMAL", faixa("0"));
        assertEquals("NORMAL", faixa("70.00"));
    }

    @Test
    void de71a89eAtencao() {
        assertEquals("ATENCAO", faixa("70.01"));
        assertEquals("ATENCAO", faixa("80"));
        assertEquals("ATENCAO", faixa("89.50"));
    }

    @Test
    void de90a100eRisco() {
        assertEquals("RISCO", faixa("90"));
        assertEquals("RISCO", faixa("100"));
    }

    @Test
    void acimaDe100eEstourado() {
        assertEquals("ESTOURADO", faixa("100.01"));
        assertEquals("ESTOURADO", faixa("250"));
    }
}
