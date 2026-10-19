package com.gestaofinanceira.orcamento.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.gestaofinanceira.orcamento.dto.OrcamentoRequest;
import com.gestaofinanceira.orcamento.dto.OrcamentoResponse;
import com.gestaofinanceira.orcamento.service.OrcamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orcamentos")
@Validated
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @PostMapping
    public ResponseEntity<OrcamentoResponse> criar(
            @Valid @RequestBody OrcamentoRequest request,
            @AuthenticationPrincipal Long usuarioId) {

        OrcamentoResponse response =
                orcamentoService.criar(request, usuarioId);

        return ResponseEntity
                .created(URI.create("/api/orcamentos/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {

        return ResponseEntity.ok(orcamentoService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Long usuarioId) {

        return ResponseEntity.ok(
                orcamentoService.buscarPorId(id, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrcamentoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrcamentoRequest request,
            @AuthenticationPrincipal Long usuarioId) {

        return ResponseEntity.ok(
                orcamentoService.atualizar(id, request, usuarioId));
    }
}