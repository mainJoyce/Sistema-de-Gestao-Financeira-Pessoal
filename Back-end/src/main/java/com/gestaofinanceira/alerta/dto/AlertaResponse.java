package com.gestaofinanceira.alerta.dto;

import java.time.LocalDateTime;

import com.gestaofinanceira.alerta.entity.Alerta;
import com.gestaofinanceira.alerta.entity.TipoAlerta;

public record AlertaResponse(
        Long id,
        TipoAlerta tipo,
        String titulo,
        String mensagem,
        boolean lida,
        LocalDateTime dataCriacao,
        LocalDateTime dataLeitura
) {

    public static AlertaResponse de(Alerta alerta) {
        return new AlertaResponse(
                alerta.getId(),
                alerta.getTipo(),
                alerta.getTitulo(),
                alerta.getMensagem(),
                alerta.isLida(),
                alerta.getDataCriacao(),
                alerta.getDataLeitura());
    }
}
