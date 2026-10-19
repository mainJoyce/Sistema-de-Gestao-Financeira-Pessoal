package com.gestaofinanceira.historico.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.historico.dto.HistoricoResponse;
import com.gestaofinanceira.historico.service.HistoricoTransacaoService;

@RestController
@RequestMapping("/api/historicos")
public class HistoricoTransacaoController {

    private final HistoricoTransacaoService historicoService;

    public HistoricoTransacaoController(HistoricoTransacaoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping
    public ResponseEntity<List<HistoricoResponse>> listar(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false) Long transacaoId) {
        return ResponseEntity.ok(historicoService.listar(usuarioId, transacaoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoricoResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(historicoService.buscarPorId(id, usuarioId));
    }
}
