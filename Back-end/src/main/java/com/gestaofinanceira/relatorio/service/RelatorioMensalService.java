package com.gestaofinanceira.relatorio.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.dashboard.service.Variacoes;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.relatorio.dto.RelatorioResponse;
import com.gestaofinanceira.relatorio.entity.RelatorioMensal;
import com.gestaofinanceira.relatorio.repository.RelatorioMensalRepository;

@Service
public class RelatorioMensalService {

    // A coluna percentual_variacao é DECIMAL(5,2): aceita no máximo 999,99
    private static final BigDecimal LIMITE = new BigDecimal("999.99");

    private final RelatorioMensalRepository relatorioRepository;
    private final DashboardService dashboardService;

    public RelatorioMensalService(RelatorioMensalRepository relatorioRepository,
                                  DashboardService dashboardService) {
        this.relatorioRepository = relatorioRepository;
        this.dashboardService = dashboardService;
    }

    @Transactional
    public RelatorioResponse gerar(Long usuarioId, YearMonth mes) {
        YearMonth alvo = (mes != null) ? mes : YearMonth.now();
        if (alvo.isAfter(YearMonth.now())) {
            throw new RegraDeNegocioException("Não é possível gerar relatório de um mês futuro");
        }

        DashboardResponse atual = dashboardService.resumo(usuarioId, alvo);
        DashboardResponse anterior = dashboardService.resumo(usuarioId, alvo.minusMonths(1));

        if (atual.receitaTotal().signum() == 0 && atual.despesaTotal().signum() == 0) {
            throw new RegraDeNegocioException(
                    "Não há transações no mês informado para gerar o relatório");
        }

        BigDecimal variacao = null;
        BigDecimal percentual = null;
        if (anterior.despesaTotal().signum() != 0) {
            variacao = atual.despesaTotal().subtract(anterior.despesaTotal());
            percentual = limitar(Variacoes.percentual(
                    atual.despesaTotal(), anterior.despesaTotal()));
        }

        RelatorioMensal relatorio = new RelatorioMensal(
                alvo.atDay(1), atual.receitaTotal(), atual.despesaTotal(),
                atual.saldo(), variacao, percentual, usuarioId);

        return paraResponse(relatorioRepository.save(relatorio), atual.gastosPorCategoria());
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listar(Long usuarioId) {
        return relatorioRepository.listar(usuarioId).stream()
                .map(r -> paraResponse(r, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioResponse buscarPorId(Long id, Long usuarioId) {
        RelatorioMensal r = relatorioRepository.buscarPorIdDoUsuario(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Relatório com id " + id + " não encontrado"));
        List<GastoCategoriaResponse> ranking = dashboardService
                .resumo(usuarioId, YearMonth.from(r.getMesReferencia()))
                .gastosPorCategoria();
        return paraResponse(r, ranking);
    }

    private BigDecimal limitar(BigDecimal valor) {
        if (valor.compareTo(LIMITE) > 0) {
            return LIMITE;
        }
        if (valor.compareTo(LIMITE.negate()) < 0) {
            return LIMITE.negate();
        }
        return valor;
    }

    private RelatorioResponse paraResponse(RelatorioMensal r,
                                        List<GastoCategoriaResponse> categorias) {
        return new RelatorioResponse(
                r.getId(), YearMonth.from(r.getMesReferencia()),
                r.getTotalRecebido(), r.getTotalGasto(), r.getSaldo(),
                r.getVariacaoMesAnterior(), r.getPercentualVariacao(),
                r.getDataGeracao(), categorias);
    }
}