package com.gestaofinanceira.transacao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gestaofinanceira.transacao.entity.Recorrencia;
import com.gestaofinanceira.transacao.entity.TipoTransacao;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoTransacao tipo,
        String estabelecimento,
        Recorrencia recorrencia,
        Long categoriaId
) {
}