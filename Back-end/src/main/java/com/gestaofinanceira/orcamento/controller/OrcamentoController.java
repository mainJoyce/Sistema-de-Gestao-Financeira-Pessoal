package com.gestaofinanceira.orcamento.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gestaofinanceira.orcamento.dto.OrcamentoRequest;
import com.gestaofinanceira.orcamento.dto.OrcamentoResponse;
import com.gestaofinanceira.orcamento.service.OrcamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(orcamentoService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(id, usuarioId));
    }

    @PostMapping
    public ResponseEntity<OrcamentoResponse> criar(
            @Valid @RequestBody OrcamentoRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        OrcamentoResponse criado = orcamentoService.criar(request, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrcamentoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrcamentoRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(orcamentoService.atualizar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        orcamentoService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
