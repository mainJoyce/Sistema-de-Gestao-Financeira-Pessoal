package com.gestaofinanceira.alerta.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.alerta.dto.AlertaResponse;
import com.gestaofinanceira.alerta.service.AlertaService;

/**
 * Alertas sao gerados pelo sistema: o usuario so consulta e marca como lido.
 * Nao existe POST de criacao manual nem DELETE.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(defaultValue = "false") boolean apenasNaoLidos) {
        return ResponseEntity.ok(alertaService.listar(usuarioId, apenasNaoLidos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(alertaService.buscarPorId(id, usuarioId));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<AlertaResponse> marcarComoLida(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(alertaService.marcarComoLida(id, usuarioId));
    }

    /** Verifica orcamentos, gastos, receitas e reserva e cria os alertas preventivos que ainda nao existem. */
    @PostMapping("/gerar")
    public ResponseEntity<List<AlertaResponse>> gerar(@AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(alertaService.gerarAlertas(usuarioId));
    }
}
