package com.gestaofinanceira.meta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.meta.dto.MetaRequest;
import com.gestaofinanceira.meta.entity.Meta;
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
    public List<Meta> listar(@RequestParam Long usuarioId) {
        return metaService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/{id}")
    public Meta buscarPorId(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        return metaService.buscarPorId(id, usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Meta criar(
            @Valid @RequestBody MetaRequest request,
            @RequestParam Long usuarioId) {

        Meta meta = new Meta();
        meta.setNome(request.nome());
        meta.setValorObjetivo(request.valorObjetivo());
        meta.setDataDesejada(request.dataDesejada());

        return metaService.criar(meta, usuarioId);
    }

    @PutMapping("/{id}")
    public Meta atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MetaRequest request,
            @RequestParam Long usuarioId) {

        Meta dados = new Meta();
        dados.setNome(request.nome());
        dados.setValorObjetivo(request.valorObjetivo());
        dados.setDataDesejada(request.dataDesejada());

        return metaService.atualizar(id, dados, usuarioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        metaService.excluir(id, usuarioId);
    }
}