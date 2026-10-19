package com.gestaofinanceira.usuario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UsuarioResponse(
        Long id,
        String nomeCompleto,
        LocalDate dataNascimento,
        String email,
        String telefone,
        BigDecimal rendaMensal,
        int dependentes
) {
}