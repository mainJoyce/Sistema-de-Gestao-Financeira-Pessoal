package com.gestaofinanceira.dashboard.controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.EstabelecimentoGastoResponse;
import com.gestaofinanceira.dashboard.dto.EvolucaoMensalResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> resumo(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return ResponseEntity.ok(dashboardService.resumo(usuarioId, mes));
    }

    /** Evolucao mensal de receitas e despesas + economia acumulada (para os graficos). */
    @GetMapping("/evolucao")
    public ResponseEntity<EvolucaoMensalResponse> evolucao(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(defaultValue = "6") int meses) {
        return ResponseEntity.ok(dashboardService.evolucaoMensal(usuarioId, meses));
    }

    /** Maiores gastos por estabelecimento no mes. */
    @GetMapping("/estabelecimentos")
    public ResponseEntity<List<EstabelecimentoGastoResponse>> estabelecimentos(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(dashboardService.gastosPorEstabelecimento(usuarioId, mes, limite));
    }
}
