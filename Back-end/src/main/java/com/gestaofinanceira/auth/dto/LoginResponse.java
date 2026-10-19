package com.gestaofinanceira.auth.dto;

public record LoginResponse(String token, String tipo, long expiraEmSegundos) {
}