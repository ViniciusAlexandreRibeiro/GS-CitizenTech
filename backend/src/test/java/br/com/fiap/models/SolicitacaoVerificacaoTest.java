package br.com.fiap.models;

import br.com.fiap.models.enums.StatusSolicitacao;
import br.com.fiap.models.enums.TipoSolicitacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoVerificacaoTest {

    private SolicitacaoVerificacao pedidoDePix(String valor) {
        Idoso idoso = new Idoso(1L, "Maria", "15881399803", "11987650001", "Vila Mariana",
                LocalDate.of(1950, 3, 14), new BigDecimal("500"));
        SolicitacaoVerificacao s = new SolicitacaoVerificacao();
        s.setIdoso(idoso);
        s.setTipo(TipoSolicitacao.PIX);
        s.setValor(new BigDecimal(valor));
        s.setDataAbertura(LocalDateTime.of(2026, 11, 12, 10, 0));
        return s;
    }

    @Test
    void pixAcimaDoLimiteExigeAprovacao() {
        assertTrue(pedidoDePix("2000").exigeAprovacaoObrigatoria());
        assertFalse(pedidoDePix("500").exigeAprovacaoObrigatoria());
    }

    @Test
    void bloqueioRegistraGuardiaoOrientacaoEValorProtegido() {
        SolicitacaoVerificacao s = pedidoDePix("2000");
        Guardiao ana = new Guardiao(1L, "Ana", null, null, null);
        s.bloquear(ana, "É golpe.", LocalDateTime.of(2026, 11, 12, 10, 4));

        assertEquals(StatusSolicitacao.BLOQUEADA, s.getStatus());
        assertEquals("Ana", s.getGuardiaoResposta().getNome());
        assertEquals(new BigDecimal("2000"), s.getValorProtegido());
        assertEquals(4L, s.getMinutosAteResposta());
    }

    @Test
    void liberadaNaoContaComoValorProtegido() {
        SolicitacaoVerificacao s = pedidoDePix("300");
        s.liberar(new Guardiao(), "Pode seguir.", LocalDateTime.now());
        assertEquals(BigDecimal.ZERO, s.getValorProtegido());
    }

    @Test
    void naoPodeSerRespondidaDuasVezes() {
        SolicitacaoVerificacao s = pedidoDePix("100");
        s.liberar(new Guardiao(), "ok", LocalDateTime.now());
        assertThrows(IllegalStateException.class, () -> s.bloquear(new Guardiao(), "golpe", LocalDateTime.now()));
    }
}
