package com.gestaofinanceira.renda.dto;

import java.math.BigDecimal;

import com.gestaofinanceira.renda.entity.Periodicidade;
import com.gestaofinanceira.renda.entity.Renda;
import com.gestaofinanceira.renda.entity.TipoRenda;

public record RendaResponse(
        Long id,
        String nome,
        TipoRenda tipo,
        BigDecimal valorReferencia,
        boolean recorrente,
        Periodicidade periodicidade
) {

    public static RendaResponse de(Renda renda) {
        return new RendaResponse(
                renda.getId(),
                renda.getNome(),
                renda.getTipo(),
                renda.getValorReferencia(),
                renda.isRecorrente(),
                renda.getPeriodicidade());
    }
}
