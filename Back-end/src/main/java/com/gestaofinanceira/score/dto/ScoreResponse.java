package com.gestaofinanceira.score.dto;

import java.time.LocalDateTime;

import com.gestaofinanceira.score.entity.ScoreFinanceiro;
import com.gestaofinanceira.score.service.ScoreCalculadora;

/** Cada subindicador vai de 0 a 20; a soma e a pontuacao (0 a 100). */
public record ScoreResponse(
        Long id,
        int pontuacao,
        String classificacao,
        String descricao,
        int controleDespesas,
        int cumprimentoOrcamento,
        int regularidadeReceitas,
        int reservaFinanceira,
        int evolucaoMetas,
        LocalDateTime dataCalculo
) {

    public static ScoreResponse de(ScoreFinanceiro s) {
        return new ScoreResponse(
                s.getId(),
                s.getPontuacao(),
                s.getClassificacao(),
                ScoreCalculadora.descricao(s.getClassificacao()),
                valor(s.getControleDespesas()),
                valor(s.getCumprimentoOrcamento()),
                valor(s.getRegularidadeReceitas()),
                valor(s.getReservaFinanceira()),
                valor(s.getEvolucaoMetas()),
                s.getDataCalculo());
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }
}
