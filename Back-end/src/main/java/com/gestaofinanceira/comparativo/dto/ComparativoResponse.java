package com.gestaofinanceira.comparativo.dto;

import java.time.YearMonth;
import java.util.List;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;

public record ComparativoResponse(
        YearMonth mesAtual,
        YearMonth mesAnterior,
        DashboardResponse atual,
        DashboardResponse anterior,
        VariacaoResponse receita,
        VariacaoResponse despesa,
        VariacaoResponse saldo,
        List<CategoriaVariacaoResponse> categorias
) {
}