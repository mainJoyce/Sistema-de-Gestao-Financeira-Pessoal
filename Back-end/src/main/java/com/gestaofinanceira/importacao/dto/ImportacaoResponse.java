package com.gestaofinanceira.importacao.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.gestaofinanceira.importacao.entity.StatusImportacao;
import com.gestaofinanceira.importacao.entity.TipoOrigem;

public record ImportacaoResponse(
        Long id,
        StatusImportacao status,
        TipoOrigem tipoOrigem,
        String nomeArquivo,
        LocalDateTime dataCriacao,
        LocalDateTime dataConclusao,
        Integer totalImportadas,
        Integer totalDuplicadas,
        Integer totalComErro,
        List<String> erros
) {
}