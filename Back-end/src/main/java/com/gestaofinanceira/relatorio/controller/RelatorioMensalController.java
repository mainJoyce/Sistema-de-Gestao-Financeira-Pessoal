package com.gestaofinanceira.relatorio.controller;

import java.net.URI;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gestaofinanceira.relatorio.dto.RelatorioResponse;
import com.gestaofinanceira.relatorio.service.RelatorioMensalService;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioMensalController {

    private final RelatorioMensalService relatorioService;

    public RelatorioMensalController(RelatorioMensalService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @PostMapping
    public ResponseEntity<RelatorioResponse> gerar(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        RelatorioResponse gerado = relatorioService.gerar(usuarioId, mes);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(gerado.id()).toUri();
        return ResponseEntity.created(location).body(gerado);
    }

    @GetMapping
    public ResponseEntity<List<RelatorioResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(relatorioService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatorioResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(relatorioService.buscarPorId(id, usuarioId));
    }
}