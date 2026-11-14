package com.gestaofinanceira.score.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestaofinanceira.score.dto.ScoreResponse;
import com.gestaofinanceira.score.service.ScoreService;

@RestController
@RequestMapping("/api/score")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** Score de agora (recalculado). */
    @GetMapping
    public ResponseEntity<ScoreResponse> atual(@AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(scoreService.recalcular(usuarioId));
    }

    /** Evolucao do score ao longo do tempo. */
    @GetMapping("/historico")
    public ResponseEntity<List<ScoreResponse>> historico(@AuthenticationPrincipal Long usuarioId) {
        return ResponseEntity.ok(scoreService.historico(usuarioId));
    }
}
