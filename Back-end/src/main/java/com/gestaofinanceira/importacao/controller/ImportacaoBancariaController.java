package com.gestaofinanceira.importacao.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gestaofinanceira.importacao.dto.ImportacaoResponse;
import com.gestaofinanceira.importacao.service.ImportacaoBancariaService;

@RestController
@RequestMapping("/api/importacoes")
public class ImportacaoBancariaController {

    private final ImportacaoBancariaService importacaoService;

    public ImportacaoBancariaController(ImportacaoBancariaService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoResponse> importar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Long usuarioId) {
        ImportacaoResponse criada = importacaoService.importar(arquivo, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<ImportacaoResponse>> listar(
            @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(importacaoService.listar(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportacaoResponse> buscarPorId(
            @PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(importacaoService.buscarPorId(id, usuarioId));
    }
}