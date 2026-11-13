package br.com.fiap.service;

import br.com.fiap.dao.GuardiaoDAO;
import br.com.fiap.dto.CadastroGuardiaoDto;
import br.com.fiap.dto.DetalhesGuardiaoDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Guardiao;
import br.com.fiap.util.Validador;

import java.util.List;

public class GuardiaoService {

    private final GuardiaoDAO guardiaoDao = new GuardiaoDAO();

    public DetalhesGuardiaoDto cadastrar(CadastroGuardiaoDto dto) {
        Guardiao guardiao = validar(dto);
        guardiaoDao.inserir(guardiao);
        return DetalhesGuardiaoDto.de(guardiao);
    }

    public List<DetalhesGuardiaoDto> listar() {
        return guardiaoDao.listarTodos().stream().map(DetalhesGuardiaoDto::de).toList();
    }

    public DetalhesGuardiaoDto buscar(long id) {
        return DetalhesGuardiaoDto.de(guardiaoDao.buscarPorId(id));
    }

    public DetalhesGuardiaoDto atualizar(long id, CadastroGuardiaoDto dto) {
        guardiaoDao.buscarPorId(id);
        Guardiao guardiao = validar(dto);
        guardiao.setId(id);
        guardiaoDao.atualizar(guardiao);
        return DetalhesGuardiaoDto.de(guardiao);
    }

    /** Vínculos são apagados junto; nas solicitações respondidas o guardião vira "null" (histórico mantido). */
    public void remover(long id) {
        guardiaoDao.excluir(id);
    }

    Guardiao validar(CadastroGuardiaoDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados do guardião.");
        }
        Guardiao guardiao = new Guardiao();
        guardiao.setNome(Validador.textoObrigatorio(dto.getNome(), "nome", "o nome", 3, 80));
        guardiao.setCpf(Validador.cpf(dto.getCpf()));
        guardiao.setTelefone(Validador.telefone(dto.getTelefone()));
        guardiao.setEmail(Validador.email(dto.getEmail()));
        return guardiao;
    }
}
