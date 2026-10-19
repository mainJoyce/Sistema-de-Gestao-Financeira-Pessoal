package com.gestaofinanceira.usuario.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.PutMapping;


import com.gestaofinanceira.usuario.dto.UsuarioCadastroRequest;
import com.gestaofinanceira.usuario.dto.UsuarioResponse;
import com.gestaofinanceira.usuario.service.UsuarioService;
import com.gestaofinanceira.usuario.dto.UsuarioAtualizacaoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody UsuarioCadastroRequest request) {
        UsuarioResponse criado = usuarioService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> meusDados(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(usuarioService.buscarPorId(usuarioId));
    }

        @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizarMeusDados(
            @AuthenticationPrincipal Long usuarioId,
            @Valid @RequestBody UsuarioAtualizacaoRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(usuarioId, request));
    }
}