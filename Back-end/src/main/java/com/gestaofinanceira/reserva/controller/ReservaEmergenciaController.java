package com.gestaofinanceira.reserva.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.reserva.dto.ReservaEmergenciaResponse;
import com.gestaofinanceira.reserva.service.ReservaEmergenciaService;

@RestController
@RequestMapping("/api/reserva-emergencia")
public class ReservaEmergenciaController {

    private final ReservaEmergenciaService reservaService;

    public ReservaEmergenciaController(ReservaEmergenciaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<ReservaEmergenciaResponse> calcular(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(defaultValue = "6") int mesesCobertura,
            @RequestParam(defaultValue = "12") int prazoMeses) {
        return ResponseEntity.ok(reservaService.calcular(usuarioId, mesesCobertura, prazoMeses));
    }
}
