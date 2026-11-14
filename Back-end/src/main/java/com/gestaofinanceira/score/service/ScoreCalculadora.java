package com.gestaofinanceira.score.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Formulas do Score (0 a 100). Cinco criterios, cada um de 0 a 20 pontos,
 * que correspondem as 5 colunas da tabela score_financeiro:
 *  - controle de despesas (economia e controle de gastos)
 *  - cumprimento de orcamento (frequencia de estouro)
 *  - regularidade de receitas
 *  - reserva financeira (cobertura da reserva + constancia de investimentos)
 *  - evolucao das metas
 * Classe sem dependencias: facil de testar.
 */
public final class ScoreCalculadora {

    private static final BigDecimal CEM = new BigDecimal("100");

    private ScoreCalculadora() {
    }

    /** Quanto menor a fatia da renda gasta, mais pontos. */
    public static int controleDespesas(BigDecimal despesas, BigDecimal renda) {
        BigDecimal percentual = despesas.multiply(CEM).divide(renda, 2, RoundingMode.HALF_UP);
        if (percentual.compareTo(new BigDecimal("50")) <= 0) return 20;
        if (percentual.compareTo(new BigDecimal("70")) <= 0) return 16;
        if (percentual.compareTo(new BigDecimal("85")) <= 0) return 10;
        if (percentual.compareTo(CEM) <= 0) return 4;
        return 0;
    }

    /** Sem orcamentos definidos nao ha o que avaliar: pontuacao neutra (10). */
    public static int cumprimentoOrcamento(int totalOrcamentos, int dentroDoLimite) {
        if (totalOrcamentos <= 0) return 10;
        return dentroDoLimite * 20 / totalOrcamentos;
    }

    /** Meses com receita registrada entre os 3 ultimos (0 a 3). */
    public static int regularidadeReceitas(int mesesComReceita) {
        switch (mesesComReceita) {
            case 3: return 20;
            case 2: return 13;
            case 1: return 7;
            default: return 0;
        }
    }

    /** Ate 10 pontos pela cobertura da reserva e ate 10 pela constancia de investimentos. */
    public static int reservaFinanceira(boolean temDespesas, long diasCobertos, int mesesComInvestimento) {
        int cobertura = 0;
        if (temDespesas) {
            if (diasCobertos >= 180) cobertura = 10;
            else if (diasCobertos >= 90) cobertura = 7;
            else if (diasCobertos >= 30) cobertura = 4;
            else if (diasCobertos > 0) cobertura = 1;
        }
        int constancia;
        if (mesesComInvestimento >= 3) constancia = 10;
        else if (mesesComInvestimento == 2) constancia = 7;
        else if (mesesComInvestimento == 1) constancia = 3;
        else constancia = 0;
        return cobertura + constancia;
    }

    /** Media do percentual atingido das metas. Sem metas: pontuacao neutra (10). */
    public static int evolucaoMetas(List<BigDecimal> percentuaisAtingidos) {
        if (percentuaisAtingidos.isEmpty()) return 10;
        BigDecimal soma = BigDecimal.ZERO;
        for (BigDecimal p : percentuaisAtingidos) {
            soma = soma.add(p.max(BigDecimal.ZERO).min(CEM));
        }
        BigDecimal media = soma.divide(BigDecimal.valueOf(percentuaisAtingidos.size()),
                2, RoundingMode.HALF_UP);
        return media.multiply(new BigDecimal("20")).divide(CEM, 0, RoundingMode.DOWN).intValue();
    }

    /** Faixas do documento: 0-39 Critico | 40-59 Atencao | 60-79 Bom | 80-100 Excelente. */
    public static String classificar(int pontuacao) {
        if (pontuacao >= 80) return "EXCELENTE";
        if (pontuacao >= 60) return "BOM";
        if (pontuacao >= 40) return "ATENCAO";
        return "CRITICO";
    }

    /** Frase curta em linguagem simples (acessibilidade). */
    public static String descricao(String classificacao) {
        switch (classificacao) {
            case "EXCELENTE":
                return "Sua saúde financeira está excelente. Continue mantendo esses hábitos.";
            case "BOM":
                return "Sua saúde financeira está boa. Pequenos ajustes podem melhorar ainda mais.";
            case "ATENCAO":
                return "Sua saúde financeira pede atenção. Revise seus gastos e seus orçamentos.";
            default:
                return "Sua saúde financeira está em estado crítico. Veja onde reduzir gastos e comece com um passo pequeno.";
        }
    }
}
