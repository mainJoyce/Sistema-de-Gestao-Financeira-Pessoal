package com.gestaofinanceira.insight.controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.insight.dto.ExplicacaoResponse;
import com.gestaofinanceira.insight.dto.InsightResponse;
import com.gestaofinanceira.insight.service.ExplicadorFinanceiroService;
import com.gestaofinanceira.insight.service.InsightService;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService insightService;
    private final ExplicadorFinanceiroService explicadorService;

    public InsightController(InsightService insightService,
                             ExplicadorFinanceiroService explicadorService) {
        this.insightService = insightService;
        this.explicadorService = explicadorService;
    }

    @GetMapping
    public ResponseEntity<List<InsightResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(insightService.listar(usuarioId));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<InsightResponse>> listarPorTipo(
            @AuthenticationPrincipal Long usuarioId,
            @PathVariable String tipo) {
        return ResponseEntity.ok(insightService.listarPorTipo(usuarioId, tipo));
    }

    @PostMapping("/gerar")
    public ResponseEntity<List<InsightResponse>> gerar(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(insightService.gerar(usuarioId));
    }

    /** Resumo do mes em linguagem simples (usa IA se estiver configurada). */
    @GetMapping("/explicacao")
    public ResponseEntity<ExplicacaoResponse> explicar(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return ResponseEntity.ok(explicadorService.explicar(usuarioId, mes));
    }
}
