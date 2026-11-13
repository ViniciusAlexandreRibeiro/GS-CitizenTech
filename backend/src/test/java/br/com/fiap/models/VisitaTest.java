package br.com.fiap.models;

import br.com.fiap.models.enums.StatusVisita;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VisitaTest {

    private Visita visitaAs(LocalDateTime horario) {
        Visita v = new Visita();
        v.setDataAgendada(horario);
        return v;
    }

    @Test
    void codigoTemSempreSeisDigitos() {
        for (int i = 0; i < 200; i++) {
            assertTrue(Visita.gerarCodigo().matches("\\d{6}"));
        }
    }

    @Test
    void aceitaConferenciaAteDuasHorasAntesOuDepois() {
        LocalDateTime agendada = LocalDateTime.of(2026, 11, 20, 14, 0);
        Visita v = visitaAs(agendada);
        assertTrue(v.estaNaJanela(agendada.minusHours(2)));
        assertTrue(v.estaNaJanela(agendada.plusMinutes(90)));
        assertFalse(v.estaNaJanela(agendada.plusHours(2).plusMinutes(1)));
        assertFalse(v.estaNaJanela(agendada.minusHours(3)));
    }

    @Test
    void confirmaSoVisitaAgendada() {
        Visita v = visitaAs(LocalDateTime.now());
        v.confirmar();
        assertEquals(StatusVisita.CONFIRMADA, v.getStatus());
        assertFalse(v.estaPendente());
        assertThrows(IllegalStateException.class, v::confirmar);
        assertThrows(IllegalStateException.class, v::cancelar);
    }

    @Test
    void visitaCanceladaNaoPodeSerConfirmada() {
        Visita v = visitaAs(LocalDateTime.now());
        v.cancelar();
        assertFalse(v.podeSerConfirmada(LocalDateTime.now()));
        assertEquals("Visita cancelada", v.getDescricaoStatus());
    }

    @Test
    void nomeExibidoEOPrestadorOuOVisitante() {
        Visita v = new Visita();
        v.setNomeVisitante("Sônia (cuidadora)");
        assertEquals("Sônia (cuidadora)", v.getNomeExibicao());
        Prestador p = new Prestador();
        p.setNome("João Silva");
        v.setPrestador(p);
        assertEquals("João Silva", v.getNomeExibicao());
    }
}
