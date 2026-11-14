package com.gestaofinanceira.reserva.dto;

import java.math.BigDecimal;

public record ReservaEmergenciaResponse(
        BigDecimal despesaMediaMensal,
        int mesesCobertura,
        BigDecimal reservaIdeal,
        BigDecimal reservaAtual,
        BigDecimal valorFaltante,
        BigDecimal percentualAtingido,
        long diasCobertos,
        int prazoMeses,
        BigDecimal valorMensalSugerido,
        boolean possuiMetaDeReserva
) {
}
