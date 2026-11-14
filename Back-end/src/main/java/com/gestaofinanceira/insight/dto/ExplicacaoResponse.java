package com.gestaofinanceira.insight.dto;

public record ExplicacaoResponse(
        String texto,
        boolean geradoPorIa,
        String aviso
) {
}
