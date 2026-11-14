package com.gestaofinanceira.insight;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.gestaofinanceira.insight.ia.ClienteIa;

class ClienteIaTest {

    @Test
    void semChaveAIaFicaDesligadaENaoChamaNada() {
        ClienteIa cliente = new ClienteIa("", "gemini-2.0-flash", 5);
        assertFalse(cliente.habilitado());
        assertTrue(cliente.gerarTexto("qualquer coisa").isEmpty());
    }
}
