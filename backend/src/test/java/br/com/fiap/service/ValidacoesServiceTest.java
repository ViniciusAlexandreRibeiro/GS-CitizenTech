package br.com.fiap.service;

import br.com.fiap.dto.CadastroGuardiaoDto;
import br.com.fiap.dto.CadastroIdosoDto;
import br.com.fiap.dto.VinculoDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Guardiao;
import br.com.fiap.models.Idoso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Testa as validações dos services, que acontecem antes de qualquer acesso ao banco. */
class ValidacoesServiceTest {

    private CadastroIdosoDto idosoValido() {
        CadastroIdosoDto dto = new CadastroIdosoDto();
        dto.setNome("  Maria   Aparecida ");
        dto.setCpf("158.813.998-03");
        dto.setTelefone("(11) 98765-0001");
        dto.setBairro("Vila Mariana");
        dto.setDataNascimento(LocalDate.of(1950, 3, 14));
        return dto;
    }

    @Test
    void idosoValidoEhConvertidoENormalizado() {
        Idoso idoso = new IdosoService().validar(idosoValido());
        assertEquals("Maria Aparecida", idoso.getNome());
        assertEquals("15881399803", idoso.getCpf());
        assertEquals("11987650001", idoso.getTelefone());
        assertEquals(Idoso.LIMITE_PIX_PADRAO, idoso.getLimitePix());
    }

    @Test
    void cpfInvalidoApontaOCampo() {
        CadastroIdosoDto dto = idosoValido();
        dto.setCpf("123.456.789-00");
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> new IdosoService().validar(dto));
        assertEquals("cpf", e.getCampo());
        assertEquals(400, e.getStatus());
    }

    @Test
    void menorDeSessentaAnosNaoEhCadastradoComoIdoso() {
        CadastroIdosoDto dto = idosoValido();
        dto.setDataNascimento(LocalDate.now().minusYears(40));
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> new IdosoService().validar(dto));
        assertEquals("dataNascimento", e.getCampo());
    }

    @Test
    void limiteNegativoEhRecusado() {
        CadastroIdosoDto dto = idosoValido();
        dto.setLimitePix(new BigDecimal("-1"));
        assertThrows(RegraNegocioException.class, () -> new IdosoService().validar(dto));
    }

    @Test
    void guardiaoComEmailInvalidoEhRecusado() {
        CadastroGuardiaoDto dto = new CadastroGuardiaoDto();
        dto.setNome("Ana Souza");
        dto.setCpf("16897684600");
        dto.setTelefone("11991230001");
        dto.setEmail("ana@");
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> new GuardiaoService().validar(dto));
        assertEquals("email", e.getCampo());

        dto.setEmail("Ana.Souza@Exemplo.com");
        Guardiao guardiao = new GuardiaoService().validar(dto);
        assertEquals("ana.souza@exemplo.com", guardiao.getEmail());
    }

    @Test
    void prioridadeDoVinculoVaiDeUmACinco() {
        VinculoDto dto = new VinculoDto();
        dto.setIdIdoso(1L);
        dto.setIdGuardiao(1L);
        dto.setParentesco("Filha");
        dto.setPrioridade(7);
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> new VinculoService().validarDados(dto));
        assertEquals("prioridade", e.getCampo());
    }
}
