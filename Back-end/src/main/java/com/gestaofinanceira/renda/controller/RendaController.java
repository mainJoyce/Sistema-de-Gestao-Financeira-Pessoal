package com.gestaofinanceira.renda.controller;

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

import com.gestaofinanceira.renda.dto.RendaRequest;
import com.gestaofinanceira.renda.dto.RendaResponse;
import com.gestaofinanceira.renda.service.RendaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rendas")
public class RendaController {

    private final RendaService rendaService;

    public RendaController(RendaService rendaService) {
        this.rendaService = rendaService;
    }

    @GetMapping
    public ResponseEntity<List<RendaResponse>> listar(@AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(rendaService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendaResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(rendaService.buscarPorId(id, usuarioId));
    }

    @PostMapping
    public ResponseEntity<RendaResponse> criar(
            @Valid @RequestBody RendaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        RendaResponse criada = rendaService.criar(request, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody RendaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(rendaService.atualizar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        rendaService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
