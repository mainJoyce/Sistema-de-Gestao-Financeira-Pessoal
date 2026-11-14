package com.gestaofinanceira.transacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gestaofinanceira.transacao.service.ClassificadorPalavraChave;

class ClassificadorPalavraChaveTest {

    private Optional<String> categoria(String texto) {
        return ClassificadorPalavraChave.categoriaPara(texto);
    }

    @Test
    void exemplosDoDocumento() {
        assertEquals(Optional.of("Alimentação"), categoria("McDonald's"));
        assertEquals(Optional.of("Assinaturas"), categoria("Netflix"));
        assertEquals(Optional.of("Transporte"), categoria("Posto Shell"));
        assertEquals(Optional.of("Saúde"), categoria("Farmácia São João"));
    }

    @Test
    void ignoraAcentosEMaiusculas() {
        assertEquals(Optional.of("Saúde"), categoria("FARMACIA CENTRAL"));
    }

    @Test
    void amazonPrimeEAssinaturaMasAmazonEhCompra() {
        assertEquals(Optional.of("Assinaturas"), categoria("Amazon Prime"));
        assertEquals(Optional.of("Compras"), categoria("Amazon"));
        assertEquals(Optional.of("Compras"), categoria("Mercado Livre"));
    }

    @Test
    void palavraNoMeioNaoCasa() {
        assertTrue(categoria("composto").isEmpty());
    }

    @Test
    void textoDesconhecidoOuVazioNaoSugere() {
        assertTrue(categoria("Xyz Ltda").isEmpty());
        assertTrue(categoria("   ").isEmpty());
        assertTrue(categoria(null).isEmpty());
    }
}
