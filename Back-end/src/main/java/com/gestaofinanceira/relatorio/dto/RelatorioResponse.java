package com.gestaofinanceira.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;

public record RelatorioResponse(
        Long id,
        YearMonth mesReferencia,
        BigDecimal totalRecebido,
        BigDecimal totalGasto,
        BigDecimal saldo,
        BigDecimal variacaoMesAnterior,
        BigDecimal percentualVariacao,
        LocalDateTime dataGeracao,
        List<GastoCategoriaResponse> principaisCategorias
) {
}