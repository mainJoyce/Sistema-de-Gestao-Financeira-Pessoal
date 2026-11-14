package com.gestaofinanceira.recomendacao.controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.gestaofinanceira.dashboard.dto.EvolucaoMensalResponse;
import com.gestaofinanceira.recomendacao.dto.PlanoFinanceiroResponse;
import com.gestaofinanceira.recomendacao.dto.RecomendacaoInvestimentoResponse;
import com.gestaofinanceira.recomendacao.service.RecomendacaoInvestimentoService;

@RestController
@RequestMapping("/api/recomendacoes")
public class RecomendacaoInvestimentoController {

    private final RecomendacaoInvestimentoService service;

    public RecomendacaoInvestimentoController(
            RecomendacaoInvestimentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RecomendacaoInvestimentoResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {

        return ResponseEntity.ok(service.listar(usuarioId));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<RecomendacaoInvestimentoResponse>> listarPorTipo(
            @AuthenticationPrincipal Long usuarioId,
            @PathVariable String tipo) {

        return ResponseEntity.ok(
                service.listarPorTipo(usuarioId, tipo));
    }

    @PostMapping("/gerar")
    public ResponseEntity<List<RecomendacaoInvestimentoResponse>> gerar(
        @AuthenticationPrincipal Long usuarioId) {
            
        return ResponseEntity.ok(service.gerar(usuarioId));
}

    /** Plano 50/30/20 do mes: quanto o ideal e quanto foi gasto em cada faixa. */
    @GetMapping("/plano")
    public ResponseEntity<PlanoFinanceiroResponse> plano(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return ResponseEntity.ok(service.plano(usuarioId, mes));
    }

    /** Evolucao do patrimonio acumulado (economia acumulada mes a mes). */
    @GetMapping("/patrimonio")
    public ResponseEntity<EvolucaoMensalResponse> patrimonio(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(defaultValue = "12") int meses) {
        return ResponseEntity.ok(service.evolucaoPatrimonio(usuarioId, meses));
    }
}
