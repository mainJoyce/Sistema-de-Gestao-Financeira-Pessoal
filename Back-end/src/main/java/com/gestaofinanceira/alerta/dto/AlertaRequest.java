package com.gestaofinanceira.alerta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertaRequest(

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
    String titulo,

    @NotBlank(message = "A mensagem é obrigatória")
    @Size(max = 500, message = "A mensagem deve ter no máximo 500 caracteres")
    String mensagem,

    @NotNull(message = "O tipo do alerta é obrigatório")
    TipoAlerta tipo

) {
    public enum TipoAlerta {
        ORCAMENTO,
        META,
        GASTO,
        SISTEMA
    }
}
