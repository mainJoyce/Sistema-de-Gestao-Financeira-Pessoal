package com.gestaofinanceira.alerta.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.alerta.dto.AlertaResponse;
import com.gestaofinanceira.alerta.entity.Alerta;
import com.gestaofinanceira.alerta.entity.TipoAlerta;
import com.gestaofinanceira.alerta.repository.AlertaRepository;
import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.dashboard.service.Variacoes;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.orcamento.dto.OrcamentoResponse;
import com.gestaofinanceira.orcamento.service.OrcamentoService;
import com.gestaofinanceira.reserva.dto.ReservaEmergenciaResponse;
import com.gestaofinanceira.reserva.service.ReservaEmergenciaService;

@Service
public class AlertaService {

    /** Aumento de gasto por categoria que vira alerta (documento: exemplo de 35%). */
    private static final BigDecimal AUMENTO_INCOMUM = new BigDecimal("30");
    /** Gasto minimo do mes anterior para comparar (evita alerta por valores minusculos). */
    private static final BigDecimal BASE_MINIMA = new BigDecimal("50");
    /** Queda de receita (entre os dois ultimos meses fechados) que vira alerta. */
    private static final BigDecimal QUEDA_DE_RENDA = new BigDecimal("15");
    /** Reserva que cobre menos que isso (em dias) gera alerta. */
    private static final long DIAS_MINIMOS_DE_RESERVA = 90;

    private final AlertaRepository alertaRepository;
    private final OrcamentoService orcamentoService;
    private final CategoriaService categoriaService;
    private final DashboardService dashboardService;
    private final ReservaEmergenciaService reservaService;

    public AlertaService(AlertaRepository alertaRepository,
                         OrcamentoService orcamentoService,
                         CategoriaService categoriaService,
                         DashboardService dashboardService,
                         ReservaEmergenciaService reservaService) {
        this.alertaRepository = alertaRepository;
        this.orcamentoService = orcamentoService;
        this.categoriaService = categoriaService;
        this.dashboardService = dashboardService;
        this.reservaService = reservaService;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar(Long usuarioId, boolean apenasNaoLidos) {
        List<Alerta> alertas = apenasNaoLidos
                ? alertaRepository.findAllByUsuarioIdAndLidaFalseOrderByDataCriacaoDescIdDesc(usuarioId)
                : alertaRepository.findAllByUsuarioIdOrderByDataCriacaoDescIdDesc(usuarioId);
        return alertas.stream().map(AlertaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscarPorId(Long id, Long usuarioId) {
        return AlertaResponse.de(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public AlertaResponse marcarComoLida(Long id, Long usuarioId) {
        Alerta alerta = buscarOuFalhar(id, usuarioId);
        alerta.marcarComoLida();
        return AlertaResponse.de(alertaRepository.save(alerta));
    }

    /**
     * Cria os alertas preventivos que ainda nao existem neste mes (US15, documento 3.5):
     * orcamento proximo do limite ou excedido, aumento incomum de gastos, queda de renda
     * e reserva insuficiente. Devolve so os alertas novos.
     * REQUIRES_NEW: tambem roda depois do commit da transacao que disparou a verificacao.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<AlertaResponse> gerarAlertas(Long usuarioId) {
        YearMonth mes = YearMonth.now();
        LocalDateTime inicioDoMes = mes.atDay(1).atStartOfDay();
        List<AlertaResponse> novos = new ArrayList<>();

        Map<Long, String> nomes = new HashMap<>();
        for (CategoriaResponse c : categoriaService.listar(usuarioId)) {
            nomes.put(c.id(), c.nome());
        }

        // 1) orcamentos do mes
        for (OrcamentoResponse o : orcamentoService.listarDoMes(usuarioId, mes)) {
            String categoria = nomes.getOrDefault(o.categoriaId(), "categoria " + o.categoriaId());
            salvarSeNovo(montarAlertaDeOrcamento(o, categoria, usuarioId),
                    usuarioId, inicioDoMes, novos);
        }

        DashboardResponse atual = dashboardService.resumo(usuarioId, mes);
        DashboardResponse anterior = dashboardService.resumo(usuarioId, mes.minusMonths(1));

        // 2) aumento incomum de gastos por categoria (mes atual ate hoje x mes anterior inteiro)
        Map<Long, BigDecimal> gastoAnterior = new HashMap<>();
        for (GastoCategoriaResponse g : anterior.gastosPorCategoria()) {
            gastoAnterior.put(g.categoriaId(), g.valor());
        }
        for (GastoCategoriaResponse g : atual.gastosPorCategoria()) {
            BigDecimal base = gastoAnterior.get(g.categoriaId());
            if (g.categoriaId() == null || base == null || base.compareTo(BASE_MINIMA) < 0) {
                continue;
            }
            BigDecimal aumento = Variacoes.percentual(g.valor(), base);
            if (aumento != null && aumento.compareTo(AUMENTO_INCOMUM) >= 0) {
                salvarSeNovo(new Alerta(TipoAlerta.AUMENTO_INCOMUM_GASTOS,
                        "Aumento de gastos em " + g.categoria(),
                        "Seus gastos com " + g.categoria() + " aumentaram "
                                + aumento.setScale(0, RoundingMode.HALF_UP)
                                + "% em relação ao mês anterior.",
                        usuarioId), usuarioId, inicioDoMes, novos);
            }
        }

        // 3) queda de renda: compara os dois ultimos meses FECHADOS
        //    (comparar com o mes em andamento daria falsa queda no comeco do mes)
        YearMonth ultimoFechado = mes.minusMonths(1);
        DashboardResponse anteriorAoFechado = dashboardService.resumo(usuarioId, mes.minusMonths(2));
        BigDecimal receitaFechada = anterior.receitaTotal();
        BigDecimal receitaBase = anteriorAoFechado.receitaTotal();
        if (receitaBase.signum() > 0) {
            BigDecimal variacao = Variacoes.percentual(receitaFechada, receitaBase);
            if (variacao != null && variacao.negate().compareTo(QUEDA_DE_RENDA) >= 0) {
                salvarSeNovo(new Alerta(TipoAlerta.QUEDA_RENDA,
                        "Queda de receita em " + ultimoFechado,
                        "Sua receita mensal apresentou redução de "
                                + variacao.negate().setScale(0, RoundingMode.HALF_UP)
                                + "% em comparação ao período anterior.",
                        usuarioId), usuarioId, inicioDoMes, novos);
            }
        }

        // 4) reserva financeira insuficiente
        Optional<ReservaEmergenciaResponse> reserva = reservaService.tentarCalcular(
                usuarioId, ReservaEmergenciaService.MESES_COBERTURA_PADRAO,
                ReservaEmergenciaService.PRAZO_PADRAO);
        if (reserva.isPresent() && reserva.get().diasCobertos() < DIAS_MINIMOS_DE_RESERVA) {
            salvarSeNovo(new Alerta(TipoAlerta.RESERVA_INSUFICIENTE,
                    "Reserva financeira insuficiente",
                    "Sua reserva financeira atual cobre apenas "
                            + reserva.get().diasCobertos() + " dias de despesas médias.",
                    usuarioId), usuarioId, inicioDoMes, novos);
        }

        return novos;
    }

    private void salvarSeNovo(Alerta alerta, Long usuarioId, LocalDateTime inicioDoMes,
                              List<AlertaResponse> novos) {
        if (alerta == null) {
            return;
        }
        boolean jaExiste = alertaRepository
                .existsByUsuarioIdAndTipoAndTituloAndDataCriacaoGreaterThanEqual(
                        usuarioId, alerta.getTipo(), alerta.getTitulo(), inicioDoMes);
        if (!jaExiste) {
            novos.add(AlertaResponse.de(alertaRepository.save(alerta)));
        }
    }

    private Alerta montarAlertaDeOrcamento(OrcamentoResponse o, String categoria, Long usuarioId) {
        BigDecimal percentual = o.percentualUsado();
        switch (o.classificacao()) {
            case "ESTOURADO":
                return new Alerta(TipoAlerta.ORCAMENTO_EXCEDIDO,
                        "Orçamento de " + categoria + " estourado",
                        "Seu gasto com " + categoria + " ultrapassou o limite definido em R$ "
                                + o.valorGasto().subtract(o.valorLimite()).setScale(2, RoundingMode.HALF_UP) + ".",
                        usuarioId);
            case "RISCO":
                return new Alerta(TipoAlerta.ORCAMENTO_PROXIMO_LIMITE,
                        "Orçamento de " + categoria + " em risco",
                        "Você já utilizou " + percentual + "% do orçamento de "
                                + categoria + " neste mês.",
                        usuarioId);
            case "ATENCAO":
                return new Alerta(TipoAlerta.ORCAMENTO_PROXIMO_LIMITE,
                        "Orçamento de " + categoria + " em atenção",
                        "Você já utilizou " + percentual + "% do orçamento de "
                                + categoria + " neste mês.",
                        usuarioId);
            default:
                return null;
        }
    }

    private Alerta buscarOuFalhar(Long id, Long usuarioId) {
        return alertaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Alerta com id " + id + " não encontrado"));
    }
}
