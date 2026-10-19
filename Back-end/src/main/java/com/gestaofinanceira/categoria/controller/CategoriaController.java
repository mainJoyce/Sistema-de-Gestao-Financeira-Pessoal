package com.gestaofinanceira.categoria.controller;

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

import com.gestaofinanceira.categoria.dto.CategoriaRequest;
import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(categoriaService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id, usuarioId));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(
            @Valid @RequestBody CategoriaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        CategoriaResponse criada = categoriaService.criar(request, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        categoriaService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}