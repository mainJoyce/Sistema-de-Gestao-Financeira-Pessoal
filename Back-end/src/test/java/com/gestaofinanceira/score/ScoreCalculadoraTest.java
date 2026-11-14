package com.gestaofinanceira.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gestaofinanceira.score.service.ScoreCalculadora;

class ScoreCalculadoraTest {

    @Test
    void controleDeDespesasPorFaixaDaRenda() {
        BigDecimal renda = new BigDecimal("1000");
        assertEquals(20, ScoreCalculadora.controleDespesas(new BigDecimal("500"), renda));
        assertEquals(16, ScoreCalculadora.controleDespesas(new BigDecimal("700"), renda));
        assertEquals(10, ScoreCalculadora.controleDespesas(new BigDecimal("850"), renda));
        assertEquals(4, ScoreCalculadora.controleDespesas(new BigDecimal("1000"), renda));
        assertEquals(0, ScoreCalculadora.controleDespesas(new BigDecimal("1001"), renda));
    }

    @Test
    void orcamentoSemDadosEhNeutro() {
        assertEquals(10, ScoreCalculadora.cumprimentoOrcamento(0, 0));
    }

    @Test
    void orcamentoProporcionalAoCumprimento() {
        assertEquals(20, ScoreCalculadora.cumprimentoOrcamento(4, 4));
        assertEquals(10, ScoreCalculadora.cumprimentoOrcamento(4, 2));
        assertEquals(0, ScoreCalculadora.cumprimentoOrcamento(4, 0));
    }

    @Test
    void regularidadeDeReceitas() {
        assertEquals(20, ScoreCalculadora.regularidadeReceitas(3));
        assertEquals(13, ScoreCalculadora.regularidadeReceitas(2));
        assertEquals(7, ScoreCalculadora.regularidadeReceitas(1));
        assertEquals(0, ScoreCalculadora.regularidadeReceitas(0));
    }

    @Test
    void reservaSomaCoberturaEConstanciaDeInvestimentos() {
        assertEquals(20, ScoreCalculadora.reservaFinanceira(true, 200, 3));
        assertEquals(10, ScoreCalculadora.reservaFinanceira(true, 0, 3));
        assertEquals(0, ScoreCalculadora.reservaFinanceira(false, 500, 0));
    }

    @Test
    void metasSemDadosSaoNeutrasEComDadosUsamAMedia() {
        assertEquals(10, ScoreCalculadora.evolucaoMetas(List.of()));
        assertEquals(10, ScoreCalculadora.evolucaoMetas(
                List.of(new BigDecimal("100"), new BigDecimal("0"))));
        assertEquals(20, ScoreCalculadora.evolucaoMetas(List.of(new BigDecimal("250"))));
    }

    @Test
    void faixasDoDocumento() {
        assertEquals("CRITICO", ScoreCalculadora.classificar(39));
        assertEquals("ATENCAO", ScoreCalculadora.classificar(40));
        assertEquals("ATENCAO", ScoreCalculadora.classificar(59));
        assertEquals("BOM", ScoreCalculadora.classificar(60));
        assertEquals("BOM", ScoreCalculadora.classificar(79));
        assertEquals("EXCELENTE", ScoreCalculadora.classificar(80));
        assertEquals("EXCELENTE", ScoreCalculadora.classificar(100));
    }

    @Test
    void pontuacaoMaximaEh100() {
        int max = 20 + 20 + 20 + 20 + 20;
        assertEquals(100, max);
        assertEquals(20, ScoreCalculadora.controleDespesas(new BigDecimal("1"), new BigDecimal("1000")));
    }
}
