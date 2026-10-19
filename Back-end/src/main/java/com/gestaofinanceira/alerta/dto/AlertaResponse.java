package com.gestaofinanceira.alerta.dto;

import com.gestaofinanceira.alerta.entity.Alerta;
import java.time.LocalDateTime;

public record AlertaResponse(
    Long id,
    String titulo,
    String mensagem,
    String tipo,
    boolean lido,
    LocalDateTime dataCriacao
) {
    public static AlertaResponse fromEntity(Alerta alerta) {
        return new AlertaResponse(
            alerta.getId(),
            alerta.getTitulo(),
            alerta.getMensagem(),
            alerta.getTipo(),
            alerta.isLido(),
            alerta.getDataCriacao()
        );
    }
}
