package com.gestaofinanceira.historico.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestaofinanceira.historico.entity.AcaoHistorico;

public record HistoricoResponse(
        Long id,
        Long transacaoId,
        AcaoHistorico acao,
        String descricaoAnterior,
        BigDecimal valorAnterior,
        LocalDate dataAnterior,
        Long categoriaAnteriorId,
        LocalDateTime dataAcao
) {
}