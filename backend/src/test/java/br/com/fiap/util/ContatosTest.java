package br.com.fiap.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContatosTest {

    @Test
    void mesmoTelefoneDigitadoDeFormasDiferentesViraOMesmoValor() {
        assertEquals("11988771234", Contatos.normalizar("(11) 98877-1234"));
        assertEquals("11988771234", Contatos.normalizar("+55 11 98877-1234"));
        assertEquals("11988771234", Contatos.normalizar("11988771234"));
    }

    @Test
    void linkFicaSemProtocoloSemWwwEEmMinusculas() {
        assertEquals("banco-atualiza-cadastro.co", Contatos.normalizar("https://www.Banco-Atualiza-Cadastro.co/"));
    }

    @Test
    void vazioViraNulo() {
        assertNull(Contatos.normalizar("   "));
        assertNull(Contatos.normalizar(null));
    }
}
