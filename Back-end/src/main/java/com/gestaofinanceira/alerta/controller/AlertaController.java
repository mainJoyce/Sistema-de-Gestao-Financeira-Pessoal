package com.gestaofinanceira.alerta.controller;

import com.gestaofinanceira.alerta.dto.AlertaRequest;
import com.gestaofinanceira.alerta.dto.AlertaResponse;
import com.gestaofinanceira.alerta.service.AlertaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public List<AlertaResponse> listar(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "false") boolean apenasNaoLidos) {

        return alertaService.listar(usuarioId, apenasNaoLidos);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertaResponse criar(
            @Valid @RequestBody AlertaRequest request,
            @RequestParam Long usuarioId) {

        return alertaService.criar(request, usuarioId);
    }

    @PatchMapping("/{id}/lido")
    public AlertaResponse marcarComoLido(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        return alertaService.marcarComoLido(id, usuarioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        alertaService.excluir(id, usuarioId);
    }
}