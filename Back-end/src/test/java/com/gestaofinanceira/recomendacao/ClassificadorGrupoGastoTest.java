package com.gestaofinanceira.recomendacao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.gestaofinanceira.recomendacao.service.ClassificadorGrupoGasto;
import com.gestaofinanceira.recomendacao.service.GrupoGasto;

class ClassificadorGrupoGastoTest {

    @Test
    void necessidades() {
        assertEquals(GrupoGasto.NECESSIDADE, ClassificadorGrupoGasto.grupoDe("Moradia"));
        assertEquals(GrupoGasto.NECESSIDADE, ClassificadorGrupoGasto.grupoDe("Alimentação"));
        assertEquals(GrupoGasto.NECESSIDADE, ClassificadorGrupoGasto.grupoDe("Contas e Serviços"));
    }

    @Test
    void desejos() {
        assertEquals(GrupoGasto.DESEJO, ClassificadorGrupoGasto.grupoDe("Lazer"));
        assertEquals(GrupoGasto.DESEJO, ClassificadorGrupoGasto.grupoDe("Assinaturas"));
    }

    @Test
    void investimentos() {
        assertEquals(GrupoGasto.INVESTIMENTO, ClassificadorGrupoGasto.grupoDe("Investimentos"));
    }

    @Test
    void categoriaDesconhecidaOuNulaContaComoDesejo() {
        assertEquals(GrupoGasto.DESEJO, ClassificadorGrupoGasto.grupoDe("Pets"));
        assertEquals(GrupoGasto.DESEJO, ClassificadorGrupoGasto.grupoDe(null));
    }
}
