package br.com.fiap.service;

import br.com.fiap.dao.IdosoDAO;
import br.com.fiap.dao.VinculoDAO;
import br.com.fiap.dto.CadastroIdosoDto;
import br.com.fiap.dto.DetalhesIdosoDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Idoso;
import br.com.fiap.util.Validador;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class IdosoService {

    /** Estatuto da Pessoa Idosa: 60 anos ou mais. */
    public static final int IDADE_MINIMA = 60;
    private static final BigDecimal LIMITE_MAXIMO = new BigDecimal("1000000");

    private final IdosoDAO idosoDao = new IdosoDAO();
    private final VinculoDAO vinculoDao = new VinculoDAO();

    public DetalhesIdosoDto cadastrar(CadastroIdosoDto dto) {
        Idoso idoso = validar(dto);
        idosoDao.inserir(idoso);
        return DetalhesIdosoDto.de(idoso, 0);
    }

    public List<DetalhesIdosoDto> listar() {
        Map<Long, Integer> guardioes = idosoDao.contarGuardioes();
        return idosoDao.listarTodos().stream()
                .map(i -> DetalhesIdosoDto.de(i, guardioes.getOrDefault(i.getId(), 0)))
                .toList();
    }

    public DetalhesIdosoDto buscar(long id) {
        Idoso idoso = idosoDao.buscarPorId(id);
        return DetalhesIdosoDto.de(idoso, vinculoDao.contarGuardioesDoIdoso(id));
    }

    public DetalhesIdosoDto atualizar(long id, CadastroIdosoDto dto) {
        idosoDao.buscarPorId(id); // garante que existe (404)
        Idoso idoso = validar(dto);
        idoso.setId(id);
        idosoDao.atualizar(idoso);
        return DetalhesIdosoDto.de(idoso, vinculoDao.contarGuardioesDoIdoso(id));
    }

    /** Os vínculos são apagados junto (ON DELETE CASCADE); visitas e solicitações impedem a exclusão. */
    public void remover(long id) {
        idosoDao.excluir(id);
    }

    Idoso validar(CadastroIdosoDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados do idoso.");
        }
        Idoso idoso = new Idoso();
        idoso.setNome(Validador.textoObrigatorio(dto.getNome(), "nome", "o nome", 3, 80));
        idoso.setCpf(Validador.cpf(dto.getCpf()));
        idoso.setTelefone(Validador.telefone(dto.getTelefone()));
        idoso.setBairro(Validador.textoObrigatorio(dto.getBairro(), "bairro", "o bairro", 2, 60));
        idoso.setDataNascimento(Validador.dataNoPassado(dto.getDataNascimento(), "dataNascimento", "a data de nascimento"));
        if (idoso.getIdade() < IDADE_MINIMA) {
            throw new RegraNegocioException("O Guardião é para pessoas com " + IDADE_MINIMA + " anos ou mais.", "dataNascimento");
        }
        BigDecimal limite = dto.getLimitePix() == null ? Idoso.LIMITE_PIX_PADRAO : dto.getLimitePix();
        if (limite.signum() < 0 || limite.compareTo(LIMITE_MAXIMO) > 0) {
            throw new RegraNegocioException("O limite de Pix deve estar entre R$ 0 e R$ 1.000.000.", "limitePix");
        }
        idoso.setLimitePix(limite);
        return idoso;
    }
}
