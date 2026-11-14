package com.gestaofinanceira.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evolucao mensal de receitas e despesas (documento, secao 3.3).
 * economiaAcumulada = soma de (receitas - despesas) desde o inicio ate o fim do mes.
 */
public record EvolucaoMensalResponse(
        List<MesResumoResponse> meses,
        BigDecimal economiaAcumulada
) {
}
