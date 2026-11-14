package com.gestaofinanceira.insight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.insight.dto.InsightResponse;
import com.gestaofinanceira.insight.entity.Insight;
import com.gestaofinanceira.insight.repository.InsightRepository;

@Service
public class InsightService {

    private static final BigDecimal CEM = new BigDecimal("100");

    private final InsightRepository insightRepository;
    private final DashboardService dashboardService;

    public InsightService(InsightRepository insightRepository,
                          DashboardService dashboardService) {
        this.insightRepository = insightRepository;
        this.dashboardService = dashboardService;
    }

    @Transactional(readOnly = true)
    public List<InsightResponse> listar(Long usuarioId) {
        return insightRepository.findAllByUsuarioIdOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsightResponse> listarPorTipo(Long usuarioId, String tipo) {
        return insightRepository
                .findAllByUsuarioIdAndTipoOrderByDataCriacaoDesc(usuarioId, tipo.toUpperCase())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Gera os insights do mes atual. RN006: sem transacoes no mes, nao ha dados
     * suficientes para analisar. Nao repete o mesmo insight no mesmo mes.
     */
    @Transactional
    public List<InsightResponse> gerar(Long usuarioId) {
        YearMonth mes = YearMonth.now();
        String periodo = mes.toString();

        DashboardResponse atual = dashboardService.resumo(usuarioId, mes);
        DashboardResponse anterior = dashboardService.resumo(usuarioId, mes.minusMonths(1));

        if (atual.receitaTotal().signum() == 0 && atual.despesaTotal().signum() == 0) {
            throw new RegraDeNegocioException(
                    "Não há transações neste mês para gerar insights");
        }

        salvar(usuarioId, "SALDO_MENSAL", periodo, "Saldo do mês",
                "Seu saldo registrado neste mês é de R$ " + atual.saldo().setScale(2, RoundingMode.HALF_UP) + ".",
                "Receitas: R$ " + atual.receitaTotal() + " | Despesas: R$ " + atual.despesaTotal());

        analisarVariacaoDeDespesas(usuarioId, periodo,
                atual.despesaTotal(), anterior.despesaTotal());

        List<GastoCategoriaResponse> categorias = atual.gastosPorCategoria();
        if (!categorias.isEmpty()) {
            GastoCategoriaResponse maior = categorias.get(0);
            salvar(usuarioId, "MAIOR_CATEGORIA_GASTO", periodo, "Categoria com maior gasto",
                    "Seu maior gasto do mês é com " + maior.categoria()
                            + ": R$ " + maior.valor() + " (" + maior.percentual() + "% das despesas).",
                    "Despesas do mês: R$ " + atual.despesaTotal());
        }

        // aumento incomum (30% ou mais) em uma categoria, em relacao ao mes anterior
        Map<Long, BigDecimal> anteriorPorCategoria = new HashMap<>();
        for (GastoCategoriaResponse g : anterior.gastosPorCategoria()) {
            anteriorPorCategoria.put(g.categoriaId(), g.valor());
        }
        for (GastoCategoriaResponse g : categorias) {
            BigDecimal base = anteriorPorCategoria.get(g.categoriaId());
            if (g.categoriaId() == null || base == null || base.compareTo(new BigDecimal("50")) < 0) {
                continue;
            }
            BigDecimal aumento = g.valor().subtract(base).multiply(CEM)
                    .divide(base, 2, RoundingMode.HALF_UP);
            if (aumento.compareTo(new BigDecimal("30")) >= 0) {
                salvar(usuarioId, "AUMENTO_CATEGORIA", periodo + ":" + g.categoriaId(),
                        "Aumento de gastos em " + g.categoria(),
                        "Seus gastos com " + g.categoria() + " aumentaram "
                                + aumento.setScale(0, RoundingMode.HALF_UP)
                                + "% em relação ao mês anterior.",
                        "Mês atual: R$ " + g.valor() + " | Mês anterior: R$ " + base);
            }
        }

        return listar(usuarioId);
    }

    private void analisarVariacaoDeDespesas(Long usuarioId, String periodo,
                                            BigDecimal atual, BigDecimal anterior) {
        if (anterior.signum() <= 0) {
            return; // sem base de comparacao
        }
        BigDecimal diferenca = atual.subtract(anterior);
        if (diferenca.signum() == 0) {
            return;
        }
        BigDecimal percentual = diferenca.abs().multiply(CEM)
                .divide(anterior, 2, RoundingMode.HALF_UP);
        String detalhe = "Mês atual: R$ " + atual + " | Mês anterior: R$ " + anterior;

        if (diferenca.signum() > 0) {
            salvar(usuarioId, "AUMENTO_DESPESAS", periodo, "Aumento nas despesas",
                    "Suas despesas aumentaram " + percentual + "% em relação ao mês anterior.",
                    detalhe);
        } else {
            salvar(usuarioId, "REDUCAO_DESPESAS", periodo, "Redução nas despesas",
                    "Suas despesas diminuíram " + percentual + "% em relação ao mês anterior.",
                    detalhe);
        }
    }

    private void salvar(Long usuarioId, String tipo, String periodo,
                        String titulo, String descricao, String detalhe) {
        if (insightRepository.existsByUsuarioIdAndTipoAndDadosReferencia(
                usuarioId, tipo, periodo)) {
            return;
        }
        String justificativa = "Análise baseada nas transações registradas. " + detalhe;
        insightRepository.save(new Insight(
                usuarioId, tipo, titulo, descricao, justificativa, periodo));
    }

    private InsightResponse paraResponse(Insight insight) {
        return new InsightResponse(
                insight.getId(),
                insight.getTitulo(),
                insight.getDescricao(),
                insight.getTipo(),
                insight.getJustificativa(),
                insight.getDadosReferencia(),
                insight.getDataCriacao());
    }
}
