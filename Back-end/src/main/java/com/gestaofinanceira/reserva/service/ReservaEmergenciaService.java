package com.gestaofinanceira.reserva.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.meta.dto.MetaResponse;
import com.gestaofinanceira.meta.service.MetaService;
import com.gestaofinanceira.reserva.dto.ReservaEmergenciaResponse;

/**
 * Reserva de emergencia (US22).
 * Ideal = despesa media mensal x meses de cobertura (padrao 6).
 * Reserva atual = soma do valor acumulado das metas cujo nome contem "reserva".
 * Despesa media = media dos ultimos 3 meses (incluindo o atual) que tiveram despesas.
 */
@Service
public class ReservaEmergenciaService {

    public static final int MESES_COBERTURA_PADRAO = 6;
    public static final int PRAZO_PADRAO = 12;

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal DIAS_NO_MES = new BigDecimal("30");

    private final DashboardService dashboardService;
    private final MetaService metaService;

    public ReservaEmergenciaService(DashboardService dashboardService, MetaService metaService) {
        this.dashboardService = dashboardService;
        this.metaService = metaService;
    }

    @Transactional(readOnly = true)
    public ReservaEmergenciaResponse calcular(Long usuarioId, int mesesCobertura, int prazoMeses) {
        if (mesesCobertura < 1 || mesesCobertura > 24) {
            throw new RegraDeNegocioException("Os meses de cobertura devem estar entre 1 e 24");
        }
        if (prazoMeses < 1 || prazoMeses > 120) {
            throw new RegraDeNegocioException("O prazo deve estar entre 1 e 120 meses");
        }
        return tentarCalcular(usuarioId, mesesCobertura, prazoMeses)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Não há despesas registradas para calcular a reserva de emergência"));
    }

    /** Vazio quando nao ha despesas nos ultimos 3 meses (RN006: sem dados, sem analise). */
    @Transactional(readOnly = true)
    public Optional<ReservaEmergenciaResponse> tentarCalcular(Long usuarioId,
                                                              int mesesCobertura, int prazoMeses) {
        Optional<BigDecimal> media = despesaMediaMensal(usuarioId);
        if (media.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal despesaMedia = media.get();
        BigDecimal ideal = despesaMedia.multiply(BigDecimal.valueOf(mesesCobertura))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal atual = BigDecimal.ZERO.setScale(2);
        boolean possuiMeta = false;
        for (MetaResponse meta : metaService.listar(usuarioId)) {
            if (meta.nome().toLowerCase().contains("reserva")) {
                possuiMeta = true;
                atual = atual.add(meta.valorAcumulado());
            }
        }

        BigDecimal faltante = ideal.subtract(atual).max(BigDecimal.ZERO);
        BigDecimal percentual = atual.multiply(CEM).divide(ideal, 2, RoundingMode.HALF_UP);
        long dias = atual.multiply(DIAS_NO_MES).divide(despesaMedia, 0, RoundingMode.DOWN).longValue();
        BigDecimal mensal = faltante.signum() == 0
                ? BigDecimal.ZERO.setScale(2)
                : faltante.divide(BigDecimal.valueOf(prazoMeses), 2, RoundingMode.CEILING);

        return Optional.of(new ReservaEmergenciaResponse(
                despesaMedia, mesesCobertura, ideal, atual, faltante,
                percentual, dias, prazoMeses, mensal, possuiMeta));
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> despesaMediaMensal(Long usuarioId) {
        YearMonth mes = YearMonth.now();
        BigDecimal soma = BigDecimal.ZERO;
        int meses = 0;
        for (int i = 0; i < 3; i++) {
            DashboardResponse resumo = dashboardService.resumo(usuarioId, mes.minusMonths(i));
            if (resumo.despesaTotal().signum() > 0) {
                soma = soma.add(resumo.despesaTotal());
                meses++;
            }
        }
        if (meses == 0) {
            return Optional.empty();
        }
        return Optional.of(soma.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP));
    }
}
