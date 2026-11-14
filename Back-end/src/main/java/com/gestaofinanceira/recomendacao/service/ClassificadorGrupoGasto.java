package com.gestaofinanceira.recomendacao.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Regra 50/30/20 (documento, secao 3.7): agrupa as categorias em necessidades,
 * desejos e investimentos. O documento nao diz quais categorias sao necessidades
 * ou desejos: este agrupamento e PROPOSTA da equipe. Categoria desconhecida
 * (criada pelo usuario) conta como desejo.
 */
public final class ClassificadorGrupoGasto {

    private static final Set<String> NECESSIDADES = Set.of(
            "moradia", "alimentacao", "transporte", "saude",
            "educacao", "contas e servicos", "impostos");

    private static final Set<String> INVESTIMENTOS = Set.of("investimentos");

    private ClassificadorGrupoGasto() {
    }

    public static GrupoGasto grupoDe(String nomeCategoria) {
        if (nomeCategoria == null) {
            return GrupoGasto.DESEJO;
        }
        String nome = Normalizer.normalize(nomeCategoria, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        if (NECESSIDADES.contains(nome)) {
            return GrupoGasto.NECESSIDADE;
        }
        if (INVESTIMENTOS.contains(nome)) {
            return GrupoGasto.INVESTIMENTO;
        }
        return GrupoGasto.DESEJO;
    }
}
