package com.gestaofinanceira.meta.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gestaofinanceira.meta.dto.AporteRequest;
import com.gestaofinanceira.meta.dto.MetaRequest;
import com.gestaofinanceira.meta.dto.MetaResponse;
import com.gestaofinanceira.meta.service.MetaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    public ResponseEntity<List<MetaResponse>> listar(@AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(metaService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetaResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(metaService.buscarPorId(id, usuarioId));
    }

    @PostMapping
    public ResponseEntity<MetaResponse> criar(
            @Valid @RequestBody MetaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        MetaResponse criada = metaService.criar(request, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MetaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(metaService.atualizar(id, request, usuarioId));
    }

    @PatchMapping("/{id}/aporte")
    public ResponseEntity<MetaResponse> registrarAporte(
            @PathVariable Long id,
            @Valid @RequestBody AporteRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(metaService.registrarAporte(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        metaService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
