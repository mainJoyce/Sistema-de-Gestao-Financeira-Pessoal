package com.gestaofinanceira.recomendacao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.recomendacao.dto.FaixaPlanoResponse;
import com.gestaofinanceira.recomendacao.dto.PlanoFinanceiroResponse;
import com.gestaofinanceira.renda.dto.RendaResponse;
import com.gestaofinanceira.renda.service.RendaService;
import com.gestaofinanceira.usuario.dto.UsuarioResponse;
import com.gestaofinanceira.usuario.service.UsuarioService;

/** Plano 50/30/20 sobre a renda atual do usuario (documento, secao 3.7 e RN007). */
@Service
public class PlanoFinanceiroService {

    private static final BigDecimal CEM = new BigDecimal("100");

    private final DashboardService dashboardService;
    private final RendaService rendaService;
    private final UsuarioService usuarioService;

    public PlanoFinanceiroService(DashboardService dashboardService,
                                  RendaService rendaService,
                                  UsuarioService usuarioService) {
        this.dashboardService = dashboardService;
        this.rendaService = rendaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Renda atual: soma do valor de referencia das fontes de renda ativas;
     * se nao houver, a renda mensal do cadastro. Sem renda, nao ha recomendacao (RN007).
     */
    @Transactional(readOnly = true)
    public BigDecimal rendaBase(Long usuarioId) {
        BigDecimal soma = BigDecimal.ZERO;
        for (RendaResponse r : rendaService.listar(usuarioId)) {
            if (r.valorReferencia() != null) {
                soma = soma.add(r.valorReferencia());
            }
        }
        if (soma.signum() <= 0) {
            UsuarioResponse usuario = usuarioService.buscarPorId(usuarioId);
            soma = usuario.rendaMensal() == null ? BigDecimal.ZERO : usuario.rendaMensal();
        }
        if (soma.signum() <= 0) {
            throw new RegraDeNegocioException(
                    "Cadastre sua renda para receber recomendações personalizadas");
        }
        return soma.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public PlanoFinanceiroResponse montar(Long usuarioId, YearMonth mes) {
        YearMonth alvo = (mes != null) ? mes : YearMonth.now();
        BigDecimal renda = rendaBase(usuarioId);
        DashboardResponse resumo = dashboardService.resumo(usuarioId, alvo);

        BigDecimal necessidades = BigDecimal.ZERO;
        BigDecimal desejos = BigDecimal.ZERO;
        BigDecimal investimentos = BigDecimal.ZERO;
        for (GastoCategoriaResponse g : resumo.gastosPorCategoria()) {
            switch (ClassificadorGrupoGasto.grupoDe(g.categoria())) {
                case NECESSIDADE:
                    necessidades = necessidades.add(g.valor());
                    break;
                case INVESTIMENTO:
                    investimentos = investimentos.add(g.valor());
                    break;
                default:
                    desejos = desejos.add(g.valor());
            }
        }

        return new PlanoFinanceiroResponse(
                alvo, renda,
                faixa(GrupoGasto.NECESSIDADE, 50, renda, necessidades),
                faixa(GrupoGasto.DESEJO, 30, renda, desejos),
                faixa(GrupoGasto.INVESTIMENTO, 20, renda, investimentos));
    }

    private FaixaPlanoResponse faixa(GrupoGasto grupo, int percentualIdeal,
                                     BigDecimal renda, BigDecimal gasto) {
        BigDecimal ideal = renda.multiply(BigDecimal.valueOf(percentualIdeal))
                .divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal percentualDaRenda = gasto.multiply(CEM)
                .divide(renda, 2, RoundingMode.HALF_UP);

        String situacao;
        if (grupo == GrupoGasto.INVESTIMENTO) {
            situacao = gasto.compareTo(ideal) >= 0 ? "META_ATINGIDA" : "ABAIXO_DO_IDEAL";
        } else {
            situacao = gasto.compareTo(ideal) > 0 ? "ACIMA_DO_IDEAL" : "DENTRO_DO_IDEAL";
        }
        return new FaixaPlanoResponse(
                grupo.name(), BigDecimal.valueOf(percentualIdeal), ideal,
                gasto.setScale(2, RoundingMode.HALF_UP), percentualDaRenda,
                ideal.subtract(gasto).setScale(2, RoundingMode.HALF_UP), situacao);
    }
}
