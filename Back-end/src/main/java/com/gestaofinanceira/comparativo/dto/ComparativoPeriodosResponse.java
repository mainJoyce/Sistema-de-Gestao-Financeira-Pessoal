package com.gestaofinanceira.comparativo.dto;

import java.time.LocalDate;
import java.util.List;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;

public record ComparativoPeriodosResponse(
        LocalDate inicioAtual,
        LocalDate fimAtual,
        LocalDate inicioAnterior,
        LocalDate fimAnterior,
        DashboardResponse atual,
        DashboardResponse anterior,
        VariacaoResponse receita,
        VariacaoResponse despesa,
        VariacaoResponse saldo,
        List<CategoriaVariacaoResponse> categorias
) {
}
