package br.com.fiap.service;

import br.com.fiap.dao.GuardiaoDAO;
import br.com.fiap.dao.IdosoDAO;
import br.com.fiap.dao.VinculoDAO;
import br.com.fiap.dto.DetalhesVinculoDto;
import br.com.fiap.dto.VinculoDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Guardiao;
import br.com.fiap.models.Idoso;
import br.com.fiap.models.VinculoGuardiao;
import br.com.fiap.util.Validador;

import java.util.List;

/** Regras da rede de proteção: quem cuida de quem. */
public class VinculoService {

    public static final int MAXIMO_GUARDIOES = 5;

    private final VinculoDAO vinculoDao = new VinculoDAO();
    private final IdosoDAO idosoDao = new IdosoDAO();
    private final GuardiaoDAO guardiaoDao = new GuardiaoDAO();

    public DetalhesVinculoDto vincular(VinculoDto dto) {
        validarDados(dto);
        Idoso idoso = idosoDao.buscarPorId(dto.getIdIdoso());
        Guardiao guardiao = guardiaoDao.buscarPorId(dto.getIdGuardiao());
        if (vinculoDao.existe(idoso.getId(), guardiao.getId())) {
            throw new RegraNegocioException(RegraNegocioException.CONFLITO,
                    "Este guardião já está vinculado a este idoso.", "idGuardiao");
        }
        if (vinculoDao.contarGuardioesDoIdoso(idoso.getId()) >= MAXIMO_GUARDIOES) {
            throw new RegraNegocioException(RegraNegocioException.CONFLITO,
                    "Cada idoso pode ter no máximo " + MAXIMO_GUARDIOES + " guardiões.", "idIdoso");
        }
        VinculoGuardiao vinculo = new VinculoGuardiao(idoso, guardiao, dto.getParentesco().trim(), dto.getPrioridade(), null);
        vinculoDao.inserir(vinculo);
        return DetalhesVinculoDto.de(vinculoDao.buscar(idoso.getId(), guardiao.getId()));
    }

    public List<DetalhesVinculoDto> listar(Long idIdoso, Long idGuardiao) {
        return vinculoDao.listar(idIdoso, idGuardiao).stream().map(DetalhesVinculoDto::de).toList();
    }

    public DetalhesVinculoDto buscar(long idIdoso, long idGuardiao) {
        return DetalhesVinculoDto.de(vinculoDao.buscar(idIdoso, idGuardiao));
    }

    /** Só parentesco e prioridade podem mudar; para trocar as pessoas, exclua e crie outro vínculo. */
    public DetalhesVinculoDto atualizar(long idIdoso, long idGuardiao, VinculoDto dto) {
        VinculoGuardiao vinculo = vinculoDao.buscar(idIdoso, idGuardiao);
        dto.setIdIdoso(idIdoso);
        dto.setIdGuardiao(idGuardiao);
        validarDados(dto);
        vinculo.setParentesco(dto.getParentesco().trim());
        vinculo.setPrioridade(dto.getPrioridade());
        vinculoDao.atualizar(vinculo);
        return DetalhesVinculoDto.de(vinculoDao.buscar(idIdoso, idGuardiao));
    }

    public void remover(long idIdoso, long idGuardiao) {
        vinculoDao.excluir(idIdoso, idGuardiao);
    }

    void validarDados(VinculoDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados do vínculo.");
        }
        Validador.idObrigatorio(dto.getIdIdoso(), "idIdoso", "o idoso");
        Validador.idObrigatorio(dto.getIdGuardiao(), "idGuardiao", "o guardião");
        dto.setParentesco(Validador.textoObrigatorio(dto.getParentesco(), "parentesco", "o parentesco", 2, 30));
        Integer prioridade = dto.getPrioridade();
        if (prioridade == null || prioridade < VinculoGuardiao.PRIORIDADE_MINIMA || prioridade > VinculoGuardiao.PRIORIDADE_MAXIMA) {
            throw new RegraNegocioException("A prioridade deve ser de 1 a 5 (1 = avisado primeiro).", "prioridade");
        }
    }
}
