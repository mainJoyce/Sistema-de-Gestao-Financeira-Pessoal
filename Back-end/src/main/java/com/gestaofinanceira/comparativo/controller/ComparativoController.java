package com.gestaofinanceira.comparativo.controller;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.comparativo.dto.ComparativoResponse;
import com.gestaofinanceira.comparativo.service.ComparativoService;

@RestController
@RequestMapping("/api/comparativos")
public class ComparativoController {

    private final ComparativoService comparativoService;

    public ComparativoController(ComparativoService comparativoService) {
        this.comparativoService = comparativoService;
    }

    @GetMapping
    public ResponseEntity<ComparativoResponse> comparar(
            @AuthenticationPrincipal Long usuarioId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth comparadoCom) {
        return ResponseEntity.ok(comparativoService.comparar(usuarioId, mes, comparadoCom));
    }
}
