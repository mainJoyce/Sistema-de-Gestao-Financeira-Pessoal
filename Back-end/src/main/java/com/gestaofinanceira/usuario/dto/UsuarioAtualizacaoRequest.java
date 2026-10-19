package com.gestaofinanceira.usuario.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoRequest(

        @NotBlank(message = "O nome completo é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nomeCompleto,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotNull(message = "A renda mensal é obrigatória")
        @DecimalMin(value = "0.00", message = "A renda mensal não pode ser negativa")
        BigDecimal rendaMensal,

        @Min(value = 0, message = "O número de dependentes não pode ser negativo")
        int dependentes
) {
}
