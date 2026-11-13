package br.com.fiap.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentosTest {

    @Test
    void aceitaCpfValidoComOuSemPontuacao() {
        assertTrue(Documentos.cpfValido("15881399803"));
        assertTrue(Documentos.cpfValido("158.813.998-03"));
    }

    @Test
    void recusaCpfComDigitoErradoOuRepetido() {
        assertFalse(Documentos.cpfValido("15881399804"));
        assertFalse(Documentos.cpfValido("11111111111"));
        assertFalse(Documentos.cpfValido("123"));
        assertFalse(Documentos.cpfValido(null));
    }

    @Test
    void validaCnpj() {
        assertTrue(Documentos.cnpjValido("84.162.294/0001-48"));
        assertFalse(Documentos.cnpjValido("84162294000149"));
        assertFalse(Documentos.cnpjValido("00000000000000"));
    }

    @Test
    void mascaraEscondeInicioEFimDoCpf() {
        assertEquals("***.813.998-**", Documentos.mascararCpf("15881399803"));
    }

    @Test
    void formataCnpj() {
        assertEquals("84.162.294/0001-48", Documentos.formatarCnpj("84162294000148"));
    }
}
