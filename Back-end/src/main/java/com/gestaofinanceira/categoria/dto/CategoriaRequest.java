package com.gestaofinanceira.categoria.dto;

import com.gestaofinanceira.categoria.entity.TipoCategoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 10, message = "O ícone deve ter no máximo 10 caracteres")
        String icone,

        @NotNull(message = "O tipo é obrigatório (RECEITA ou DESPESA)")
        TipoCategoria tipo
) {
}