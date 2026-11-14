package com.gestaofinanceira.transacao.event;

/**
 * Publicado quando as transacoes de um usuario mudam (criar, editar, excluir, importar).
 * Quem precisa reagir (alertas, score) escuta este evento, sem o modulo de
 * transacao precisar conhecer esses modulos.
 */
public record TransacaoAlteradaEvent(Long usuarioId) {
}
