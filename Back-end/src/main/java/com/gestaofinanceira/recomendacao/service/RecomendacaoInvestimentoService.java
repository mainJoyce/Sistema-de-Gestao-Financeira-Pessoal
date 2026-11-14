package com.gestaofinanceira.recomendacao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.EvolucaoMensalResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.recomendacao.dto.FaixaPlanoResponse;
import com.gestaofinanceira.recomendacao.dto.PlanoFinanceiroResponse;
import com.gestaofinanceira.recomendacao.dto.RecomendacaoInvestimentoResponse;
import com.gestaofinanceira.recomendacao.entity.RecomendacaoInvestimento;
import com.gestaofinanceira.recomendacao.repository.RecomendacaoInvestimentoRepository;
import com.gestaofinanceira.reserva.dto.ReservaEmergenciaResponse;
import com.gestaofinanceira.reserva.service.ReservaEmergenciaService;

/**
 * Recomendacoes educativas (documento 3.7 e 3.8), sempre baseadas na renda atual (RN007):
 * percentual minimo de investimento, reserva ideal, plano 50/30/20 e metas sugeridas.
 * Calculos feitos pelo backend, sem garantia de resultado.
 */
@Service
public class RecomendacaoInvestimentoService {

    private static final String VERSAO_REGRA = "2.0";
    private static final String AVISO = " Sugestão educativa, sem garantia de resultado.";
    private static final BigDecimal PERCENTUAL_MINIMO_INVESTIMENTO = new BigDecimal("20");
    private static final BigDecimal CEM = new BigDecimal("100");
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final RecomendacaoInvestimentoRepository repository;
    private final DashboardService dashboardService;
    private final PlanoFinanceiroService planoService;
    private final ReservaEmergenciaService reservaService;

    public RecomendacaoInvestimentoService(RecomendacaoInvestimentoRepository repository,
                                           DashboardService dashboardService,
                                           PlanoFinanceiroService planoService,
                                           ReservaEmergenciaService reservaService) {
        this.repository = repository;
        this.dashboardService = dashboardService;
        this.planoService = planoService;
        this.reservaService = reservaService;
    }

    @Transactional(readOnly = true)
    public List<RecomendacaoInvestimentoResponse> listar(Long usuarioId) {
        return repository.findAllByUsuarioIdOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecomendacaoInvestimentoResponse> listarPorTipo(Long usuarioId, String tipo) {
        return repository
                .findAllByUsuarioIdAndTipoOrderByDataCriacaoDesc(usuarioId, tipo.toUpperCase())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanoFinanceiroResponse plano(Long usuarioId, YearMonth mes) {
        return planoService.montar(usuarioId, mes);
    }

    /** Evolucao do patrimonio acumulado (economia acumulada mes a mes). */
    @Transactional(readOnly = true)
    public EvolucaoMensalResponse evolucaoPatrimonio(Long usuarioId, int meses) {
        return dashboardService.evolucaoMensal(usuarioId, meses);
    }

    /** Gera as recomendacoes do mes (no maximo uma por titulo, por mes). */
    @Transactional
    public List<RecomendacaoInvestimentoResponse> gerar(Long usuarioId) {
        YearMonth mes = YearMonth.now();
        BigDecimal renda = planoService.rendaBase(usuarioId); // 422 se nao houver renda (RN007)
        BigDecimal minimo = renda.multiply(PERCENTUAL_MINIMO_INVESTIMENTO)
                .divide(CEM, 2, RoundingMode.HALF_UP);

        // 1) percentual minimo de investimento
        salvar(usuarioId, mes, "PERCENTUAL_INVESTIMENTO",
                "Percentual mínimo de investimento", minimo,
                "Com sua renda atual de " + moeda(renda) + ", recomenda-se destinar pelo menos "
                        + PERCENTUAL_MINIMO_INVESTIMENTO.toBigInteger() + "% ("
                        + moeda(minimo) + ") por mês a investimentos e reserva financeira.",
                "Regra 50/30/20: 20% da renda para investimentos e reserva.");

        // 2) reserva de emergencia e meta sugerida
        Optional<ReservaEmergenciaResponse> reserva = reservaService.tentarCalcular(
                usuarioId, ReservaEmergenciaService.MESES_COBERTURA_PADRAO,
                ReservaEmergenciaService.PRAZO_PADRAO);
        if (reserva.isEmpty()) {
            salvar(usuarioId, mes, "EDUCATIVA", "Registre suas despesas", null,
                    "Registre suas despesas para calcularmos o valor ideal da sua reserva de emergência.",
                    "Ainda não há despesas registradas nos últimos meses.");
        } else {
            ReservaEmergenciaResponse r = reserva.get();
            if (r.valorFaltante().signum() > 0) {
                long meses = r.valorFaltante().divide(minimo, 0, RoundingMode.CEILING).longValue();
                salvar(usuarioId, mes, "RESERVA_EMERGENCIA", "Monte sua reserva de emergência", minimo,
                        "Com sua renda atual, recomenda-se investir pelo menos " + moeda(minimo)
                                + " por mês para atingir uma reserva equivalente a "
                                + r.mesesCobertura() + " meses de despesas (" + moeda(r.reservaIdeal())
                                + ") em aproximadamente " + meses + " meses.",
                        "Despesa média mensal: " + moeda(r.despesaMediaMensal())
                                + ". Reserva atual: " + moeda(r.reservaAtual()) + ".");
            } else {
                salvar(usuarioId, mes, "RESERVA_EMERGENCIA", "Reserva de emergência completa", null,
                        "Sua reserva já cobre " + r.mesesCobertura()
                                + " meses de despesas. Considere manter esse valor guardado.",
                        "Reserva atual: " + moeda(r.reservaAtual()) + ".");
            }
            if (!r.possuiMetaDeReserva()) {
                salvar(usuarioId, mes, "META_SUGERIDA", "Crie a meta Reserva de emergência", r.reservaIdeal(),
                        "Crie uma meta chamada Reserva de emergência com objetivo de "
                                + moeda(r.reservaIdeal()) + " para acompanhar seu progresso.",
                        "Você ainda não tem uma meta de reserva cadastrada.");
            }
        }

        // 3) distribuicao 50/30/20 (so quando ha despesas no mes)
        DashboardResponse resumo = dashboardService.resumo(usuarioId, mes);
        if (resumo.despesaTotal().signum() > 0) {
            PlanoFinanceiroResponse plano = planoService.montar(usuarioId, mes);
            recomendarDistribuicao(usuarioId, mes, plano);
        }

        return listar(usuarioId);
    }

    private void recomendarDistribuicao(Long usuarioId, YearMonth mes, PlanoFinanceiroResponse plano) {
        FaixaPlanoResponse nec = plano.necessidades();
        FaixaPlanoResponse des = plano.desejos();
        FaixaPlanoResponse inv = plano.investimentos();

        if ("ACIMA_DO_IDEAL".equals(nec.situacao())) {
            salvar(usuarioId, mes, "DISTRIBUICAO_50_30_20", "Necessidades acima de 50% da renda", null,
                    "Seus gastos com necessidades estão em " + nec.percentualDaRenda()
                            + "% da renda (ideal: 50%). Revise contas fixas e procure onde economizar.",
                    "Necessidades: " + moeda(nec.valorGasto()) + " de " + moeda(nec.valorIdeal()) + " ideais.");
        } else if ("ACIMA_DO_IDEAL".equals(des.situacao())) {
            salvar(usuarioId, mes, "DISTRIBUICAO_50_30_20", "Desejos acima de 30% da renda", null,
                    "Seus gastos com desejos estão em " + des.percentualDaRenda()
                            + "% da renda (ideal: 30%). Reduzir um pouco aqui libera dinheiro para sua reserva.",
                    "Desejos: " + moeda(des.valorGasto()) + " de " + moeda(des.valorIdeal()) + " ideais.");
        } else if ("ABAIXO_DO_IDEAL".equals(inv.situacao())) {
            salvar(usuarioId, mes, "DISTRIBUICAO_50_30_20", "Invista mais neste mês", inv.valorIdeal(),
                    "Você investiu " + moeda(inv.valorGasto()) + " neste mês, e a meta de 20% da renda é "
                            + moeda(inv.valorIdeal()) + ".",
                    "Investimentos: " + inv.percentualDaRenda() + "% da renda (ideal: 20%).");
        } else {
            salvar(usuarioId, mes, "DISTRIBUICAO_50_30_20", "Distribuição equilibrada", null,
                    "Sua distribuição entre necessidades, desejos e investimentos está dentro do plano 50/30/20.",
                    "Parabéns: todas as faixas estão dentro do ideal neste mês.");
        }
    }

    private void salvar(Long usuarioId, YearMonth mes, String tipo, String titulo,
                        BigDecimal valorSugerido, String descricao, String justificativa) {
        boolean jaExiste = repository.existsByUsuarioIdAndTituloAndDataCriacaoGreaterThanEqual(
                usuarioId, titulo, mes.atDay(1).atStartOfDay());
        if (jaExiste) {
            return;
        }
        repository.save(new RecomendacaoInvestimento(
                usuarioId, tipo, titulo, limitar(descricao + AVISO, 500),
                valorSugerido, limitar(justificativa, 500), VERSAO_REGRA));
    }

    private String limitar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }

    private String moeda(BigDecimal valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    private RecomendacaoInvestimentoResponse paraResponse(RecomendacaoInvestimento r) {
        return new RecomendacaoInvestimentoResponse(
                r.getId(), r.getTipo(), r.getTitulo(), r.getDescricao(),
                r.getValorSugerido(), r.getJustificativa(), r.getVersaoRegra(),
                r.getDataCriacao());
    }
}
