package com.gestaofinanceira.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.EstabelecimentoGastoResponse;
import com.gestaofinanceira.dashboard.dto.EvolucaoMensalResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.dto.MesResumoResponse;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.repository.TransacaoRepository;

@Service
public class DashboardService {

    private static final int MAXIMO_MESES_EVOLUCAO = 24;

    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;

    public DashboardService(TransacaoRepository transacaoRepository,
                            CategoriaService categoriaService) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumo(Long usuarioId, YearMonth mes) {
        YearMonth mesConsulta = (mes != null) ? mes : YearMonth.now();
        return resumoPeriodo(usuarioId, mesConsulta.atDay(1), mesConsulta.atEndOfMonth());
    }

    /** Resumo de um periodo qualquer (usado pelo dashboard e pela comparacao de periodos). */
    @Transactional(readOnly = true)
    public DashboardResponse resumoPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        BigDecimal receita = transacaoRepository
                .somarPorTipo(usuarioId, TipoTransacao.RECEITA, inicio, fim);
        BigDecimal despesa = transacaoRepository
                .somarPorTipo(usuarioId, TipoTransacao.DESPESA, inicio, fim);
        BigDecimal saldo = receita.subtract(despesa);

        BigDecimal comprometimento = percentual(despesa, receita);

        Map<Long, String> nomes = new HashMap<>();
        for (CategoriaResponse c : categoriaService.listar(usuarioId)) {
            nomes.put(c.id(), c.nome());
        }

        List<GastoCategoriaResponse> porCategoria = new ArrayList<>();
        for (Object[] linha : transacaoRepository
                .somarDespesasPorCategoria(usuarioId, inicio, fim)) {
            Long categoriaId = (Long) linha[0];
            BigDecimal valor = (BigDecimal) linha[1];
            String nome = (categoriaId == null)
                    ? "Sem categoria"
                    : nomes.getOrDefault(categoriaId, "Categoria removida");
            porCategoria.add(new GastoCategoriaResponse(
                    categoriaId, nome, valor, percentual(valor, despesa)));
        }

        return new DashboardResponse(
                YearMonth.from(inicio), receita, despesa, saldo, comprometimento, porCategoria);
    }

    /** Evolucao dos ultimos N meses (inclui o mes atual), com a economia acumulada. */
    @Transactional(readOnly = true)
    public EvolucaoMensalResponse evolucaoMensal(Long usuarioId, int meses) {
        if (meses < 1 || meses > MAXIMO_MESES_EVOLUCAO) {
            throw new RegraDeNegocioException(
                    "Informe de 1 a " + MAXIMO_MESES_EVOLUCAO + " meses");
        }
        YearMonth atual = YearMonth.now();
        List<MesResumoResponse> lista = new ArrayList<>();
        for (int i = meses - 1; i >= 0; i--) {
            YearMonth mes = atual.minusMonths(i);
            BigDecimal receita = transacaoRepository.somarPorTipo(
                    usuarioId, TipoTransacao.RECEITA, mes.atDay(1), mes.atEndOfMonth());
            BigDecimal despesa = transacaoRepository.somarPorTipo(
                    usuarioId, TipoTransacao.DESPESA, mes.atDay(1), mes.atEndOfMonth());
            lista.add(new MesResumoResponse(
                    mes, receita, despesa, receita.subtract(despesa),
                    economiaAcumulada(usuarioId, mes.atEndOfMonth())));
        }
        return new EvolucaoMensalResponse(lista, economiaAcumulada(usuarioId, atual.atEndOfMonth()));
    }

    /** Soma de (receitas - despesas) de todas as transacoes ativas ate a data informada. */
    @Transactional(readOnly = true)
    public BigDecimal economiaAcumulada(Long usuarioId, LocalDate ate) {
        BigDecimal receitas = transacaoRepository.somarPorTipoAte(
                usuarioId, TipoTransacao.RECEITA, ate);
        BigDecimal despesas = transacaoRepository.somarPorTipoAte(
                usuarioId, TipoTransacao.DESPESA, ate);
        return receitas.subtract(despesas);
    }

    /** Maiores gastos por estabelecimento no mes (documento, secao 3.3). */
    @Transactional(readOnly = true)
    public List<EstabelecimentoGastoResponse> gastosPorEstabelecimento(
            Long usuarioId, YearMonth mes, int limite) {
        if (limite < 1 || limite > 50) {
            throw new RegraDeNegocioException("O limite deve estar entre 1 e 50");
        }
        YearMonth alvo = (mes != null) ? mes : YearMonth.now();
        BigDecimal despesa = transacaoRepository.somarPorTipo(
                usuarioId, TipoTransacao.DESPESA, alvo.atDay(1), alvo.atEndOfMonth());

        List<EstabelecimentoGastoResponse> lista = new ArrayList<>();
        for (Object[] linha : transacaoRepository.somarDespesasPorEstabelecimento(
                usuarioId, alvo.atDay(1), alvo.atEndOfMonth())) {
            if (lista.size() >= limite) {
                break;
            }
            BigDecimal valor = (BigDecimal) linha[1];
            lista.add(new EstabelecimentoGastoResponse(
                    (String) linha[0], valor, percentual(valor, despesa)));
        }
        return lista;
    }

    private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
}
