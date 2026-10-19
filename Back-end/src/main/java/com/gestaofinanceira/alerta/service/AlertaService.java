package com.gestaofinanceira.alerta.service;

import com.gestaofinanceira.alerta.dto.AlertaRequest;
import com.gestaofinanceira.alerta.dto.AlertaResponse;
import com.gestaofinanceira.alerta.entity.Alerta;
import com.gestaofinanceira.alerta.repository.AlertaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar(Long usuarioId, boolean apenasNaoLidos) {
        List<Alerta> alertas;

        if (apenasNaoLidos) {
            alertas = alertaRepository
                    .findByUsuarioIdAndLidoFalseOrderByDataCriacaoDesc(usuarioId);
        } else {
            alertas = alertaRepository
                    .findByUsuarioIdOrderByDataCriacaoDesc(usuarioId);
        }

        return alertas.stream()
                .map(AlertaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AlertaResponse criar(AlertaRequest request, Long usuarioId) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(request.titulo());
        alerta.setMensagem(request.mensagem());
        alerta.setTipo(request.tipo().name());
        alerta.setLido(false);
        alerta.setUsuarioId(usuarioId);

        Alerta salvo = alertaRepository.save(alerta);

        return AlertaResponse.fromEntity(salvo);
    }

    @Transactional
    public AlertaResponse marcarComoLido(Long id, Long usuarioId) {
        Alerta alerta = alertaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Alerta não encontrado"));

        alerta.setLido(true);

        return AlertaResponse.fromEntity(alertaRepository.save(alerta));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Alerta alerta = alertaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Alerta não encontrado"));

        alertaRepository.delete(alerta);
    }
}