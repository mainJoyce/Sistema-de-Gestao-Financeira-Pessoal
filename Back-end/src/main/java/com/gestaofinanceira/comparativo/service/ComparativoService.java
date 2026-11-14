package com.gestaofinanceira.comparativo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.comparativo.dto.CategoriaVariacaoResponse;
import com.gestaofinanceira.comparativo.dto.ComparativoPeriodosResponse;
import com.gestaofinanceira.comparativo.dto.ComparativoResponse;
import com.gestaofinanceira.comparativo.dto.VariacaoResponse;
import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.dashboard.service.Variacoes;
import com.gestaofinanceira.exception.RegraDeNegocioException;

@Service
public class ComparativoService {

    private final DashboardService dashboardService;

    public ComparativoService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Transactional(readOnly = true)
    public ComparativoResponse comparar(Long usuarioId, YearMonth mes, YearMonth comparadoCom) {
        YearMonth mesAtual = (mes != null) ? mes : YearMonth.now();
        YearMonth mesAnterior = (comparadoCom != null) ? comparadoCom : mesAtual.minusMonths(1);

        if (mesAtual.equals(mesAnterior)) {
            throw new RegraDeNegocioException("Os dois períodos comparados devem ser diferentes");
        }

        DashboardResponse atual = dashboardService.resumo(usuarioId, mesAtual);
        DashboardResponse anterior = dashboardService.resumo(usuarioId, mesAnterior);

        return new ComparativoResponse(
                mesAtual, mesAnterior, atual, anterior,
                variacao(atual.receitaTotal(), anterior.receitaTotal()),
                variacao(atual.despesaTotal(), anterior.despesaTotal()),
                variacao(atual.saldo(), anterior.saldo()),
                compararCategorias(atual, anterior));
    }

    /** Compara dois periodos livres (semana, trimestre, etc.), de ate 366 dias cada. */
    @Transactional(readOnly = true)
    public ComparativoPeriodosResponse compararPeriodos(Long usuarioId,
                                                        LocalDate inicioAtual, LocalDate fimAtual,
                                                        LocalDate inicioAnterior, LocalDate fimAnterior) {
        validarPeriodo(inicioAtual, fimAtual, "atual");
        validarPeriodo(inicioAnterior, fimAnterior, "anterior");
        if (inicioAtual.equals(inicioAnterior) && fimAtual.equals(fimAnterior)) {
            throw new RegraDeNegocioException("Os dois períodos comparados devem ser diferentes");
        }

        DashboardResponse atual = dashboardService.resumoPeriodo(usuarioId, inicioAtual, fimAtual);
        DashboardResponse anterior = dashboardService.resumoPeriodo(usuarioId, inicioAnterior, fimAnterior);

        return new ComparativoPeriodosResponse(
                inicioAtual, fimAtual, inicioAnterior, fimAnterior, atual, anterior,
                variacao(atual.receitaTotal(), anterior.receitaTotal()),
                variacao(atual.despesaTotal(), anterior.despesaTotal()),
                variacao(atual.saldo(), anterior.saldo()),
                compararCategorias(atual, anterior));
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim, String nome) {
        if (inicio.isAfter(fim)) {
            throw new RegraDeNegocioException(
                    "No período " + nome + ", a data inicial não pode ser posterior à final");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > 365) {
            throw new RegraDeNegocioException(
                    "O período " + nome + " pode ter no máximo 366 dias");
        }
    }

    private List<CategoriaVariacaoResponse> compararCategorias(DashboardResponse atual,
                                                                DashboardResponse anterior) {
        Map<Long, GastoCategoriaResponse> mapaAtual = indexar(atual.gastosPorCategoria());
        Map<Long, GastoCategoriaResponse> mapaAnterior = indexar(anterior.gastosPorCategoria());

        Set<Long> ids = new LinkedHashSet<>(mapaAtual.keySet());
        ids.addAll(mapaAnterior.keySet());

        List<CategoriaVariacaoResponse> lista = new ArrayList<>();
        for (Long id : ids) {
            GastoCategoriaResponse a = mapaAtual.get(id);
            GastoCategoriaResponse b = mapaAnterior.get(id);
            BigDecimal valorAtual = (a != null) ? a.valor() : BigDecimal.ZERO;
            BigDecimal valorAnterior = (b != null) ? b.valor() : BigDecimal.ZERO;
            String nome = (a != null) ? a.categoria() : b.categoria();
            lista.add(new CategoriaVariacaoResponse(
                    id, nome, valorAtual, valorAnterior, variacao(valorAtual, valorAnterior)));
        }
        return lista;
    }

    private Map<Long, GastoCategoriaResponse> indexar(List<GastoCategoriaResponse> itens) {
        Map<Long, GastoCategoriaResponse> mapa = new HashMap<>();
        for (GastoCategoriaResponse item : itens) {
            mapa.put(item.categoriaId(), item);
        }
        return mapa;
    }

    private VariacaoResponse variacao(BigDecimal atual, BigDecimal base) {
        return new VariacaoResponse(atual.subtract(base), Variacoes.percentual(atual, base));
    }
}