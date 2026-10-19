package com.gestaofinanceira.categoria.dto;

import com.gestaofinanceira.categoria.entity.TipoCategoria;

public record CategoriaResponse(
        Long id,
        String nome,
        String icone,
        TipoCategoria tipo,
        boolean padrao
) {
}