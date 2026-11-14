package com.gestaofinanceira.meta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaResponse(
        Long id,
        String nome,
        BigDecimal valorObjetivo,
        BigDecimal valorAcumulado,
        LocalDate dataDesejada,
        BigDecimal valorRestante,
        BigDecimal percentualAtingido,
        long mesesRestantes,
        BigDecimal valorMensalNecessario,
        boolean concluida
) {
}
