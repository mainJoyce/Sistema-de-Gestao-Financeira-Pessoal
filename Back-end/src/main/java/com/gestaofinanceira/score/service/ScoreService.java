package com.gestaofinanceira.score.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.meta.dto.MetaResponse;
import com.gestaofinanceira.meta.service.MetaService;
import com.gestaofinanceira.orcamento.dto.OrcamentoResponse;
import com.gestaofinanceira.orcamento.service.OrcamentoService;
import com.gestaofinanceira.reserva.dto.ReservaEmergenciaResponse;
import com.gestaofinanceira.reserva.service.ReservaEmergenciaService;
import com.gestaofinanceira.score.dto.ScoreResponse;
import com.gestaofinanceira.score.entity.ScoreFinanceiro;
import com.gestaofinanceira.score.repository.ScoreFinanceiroRepository;
import com.gestaofinanceira.usuario.dto.UsuarioResponse;
import com.gestaofinanceira.usuario.service.UsuarioService;

/**
 * Score de saude financeira (0 a 100). Recalculado a cada consulta e sempre que
 * as transacoes mudam (RN008). Uma nova "foto" so e gravada quando algum valor muda.
 */
@Service
public class ScoreService {

    private final ScoreFinanceiroRepository scoreRepository;
    private final DashboardService dashboardService;
    private final OrcamentoService orcamentoService;
    private final MetaService metaService;
    private final ReservaEmergenciaService reservaService;
    private final UsuarioService usuarioService;

    public ScoreService(ScoreFinanceiroRepository scoreRepository,
                        DashboardService dashboardService,
                        OrcamentoService orcamentoService,
                        MetaService metaService,
                        ReservaEmergenciaService reservaService,
                        UsuarioService usuarioService) {
        this.scoreRepository = scoreRepository;
        this.dashboardService = dashboardService;
        this.orcamentoService = orcamentoService;
        this.metaService = metaService;
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Calcula o score de agora e grava uma nova foto se algo mudou.
     * REQUIRES_NEW: tambem funciona quando chamado depois do commit de outra transacao.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScoreResponse recalcular(Long usuarioId) {
        UsuarioResponse usuario = usuarioService.buscarPorId(usuarioId);
        YearMonth mes = YearMonth.now();

        DashboardResponse m0 = dashboardService.resumo(usuarioId, mes);
        DashboardResponse m1 = dashboardService.resumo(usuarioId, mes.minusMonths(1));
        DashboardResponse m2 = dashboardService.resumo(usuarioId, mes.minusMonths(2));

        BigDecimal renda = usuario.rendaMensal();
        if (renda == null || renda.signum() <= 0) {
            renda = m0.receitaTotal();
        }
        if (renda.signum() <= 0) {
            throw new RegraDeNegocioException(
                    "Cadastre sua renda mensal ou registre receitas para calcular o Score");
        }

        int controle = ScoreCalculadora.controleDespesas(m0.despesaTotal(), renda);

        List<OrcamentoResponse> orcamentos = orcamentoService.listarDoMes(usuarioId, mes);
        int dentro = (int) orcamentos.stream()
                .filter(o -> o.valorGasto().compareTo(o.valorLimite()) <= 0)
                .count();
        int orcamento = ScoreCalculadora.cumprimentoOrcamento(orcamentos.size(), dentro);

        int mesesComReceita = 0;
        int mesesComInvestimento = 0;
        for (DashboardResponse d : List.of(m0, m1, m2)) {
            if (d.receitaTotal().signum() > 0) {
                mesesComReceita++;
            }
            if (investiuNoMes(d)) {
                mesesComInvestimento++;
            }
        }
        int receitas = ScoreCalculadora.regularidadeReceitas(mesesComReceita);

        Optional<ReservaEmergenciaResponse> reservaInfo = reservaService
                .tentarCalcular(usuarioId, ReservaEmergenciaService.MESES_COBERTURA_PADRAO,
                        ReservaEmergenciaService.PRAZO_PADRAO);
        long dias = reservaInfo.map(ReservaEmergenciaResponse::diasCobertos).orElse(0L);
        int reserva = ScoreCalculadora.reservaFinanceira(
                reservaInfo.isPresent(), dias, mesesComInvestimento);

        List<BigDecimal> percentuais = new ArrayList<>();
        for (MetaResponse meta : metaService.listar(usuarioId)) {
            percentuais.add(meta.percentualAtingido());
        }
        int metas = ScoreCalculadora.evolucaoMetas(percentuais);

        int pontuacao = controle + orcamento + receitas + reserva + metas;

        Optional<ScoreFinanceiro> ultimo = scoreRepository
                .findFirstByUsuarioIdOrderByDataCalculoDescIdDesc(usuarioId);
        if (ultimo.isPresent()
                && ultimo.get().temOsMesmosValoresDe(pontuacao, controle, orcamento, receitas, reserva, metas)) {
            return ScoreResponse.de(ultimo.get());
        }

        ScoreFinanceiro novo = new ScoreFinanceiro(usuarioId, pontuacao,
                ScoreCalculadora.classificar(pontuacao),
                controle, orcamento, receitas, reserva, metas);
        return ScoreResponse.de(scoreRepository.save(novo));
    }

    @Transactional(readOnly = true)
    public List<ScoreResponse> historico(Long usuarioId) {
        return scoreRepository.findAllByUsuarioIdOrderByDataCalculoDescIdDesc(usuarioId)
                .stream()
                .map(ScoreResponse::de)
                .toList();
    }

    private boolean investiuNoMes(DashboardResponse resumo) {
        for (GastoCategoriaResponse g : resumo.gastosPorCategoria()) {
            if (g.categoria() != null && g.categoria().equalsIgnoreCase("Investimentos")) {
                return true;
            }
        }
        return false;
    }
}
