package br.com.fiap.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class IdosoTest {

    @Test
    void calculaIdadePelaDataDeNascimento() {
        Idoso idoso = new Idoso();
        idoso.setDataNascimento(LocalDate.now().minusYears(74).minusDays(1));
        assertEquals(74, idoso.getIdade());
    }

    @Test
    void comparaValorComOLimiteDePix() {
        Idoso idoso = new Idoso();
        idoso.setLimitePix(new BigDecimal("500.00"));
        assertTrue(idoso.excedeLimite(new BigDecimal("500.01")));
        assertFalse(idoso.excedeLimite(new BigDecimal("500")));
        assertFalse(idoso.excedeLimite(null));
    }

    @Test
    void herdaDadosDePessoaEMascaraCpf() {
        Pessoa pessoa = new Idoso(1L, "Maria", "15881399803", "11987650001", "Vila Mariana", null, null);
        assertEquals("Idoso", pessoa.getPapel());
        assertEquals("***.813.998-**", pessoa.getCpfMascarado());
    }
}
