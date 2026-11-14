package com.gestaofinanceira.transacao.dto;

/**
 * origem: HISTORICO (o usuario ja categorizou esse estabelecimento antes),
 * PALAVRA_CHAVE (reconhecido pelo nome) ou PADRAO (categoria "Outros").
 */
public record SugestaoCategoriaResponse(
        Long categoriaId,
        String categoria,
        String origem
) {
}
