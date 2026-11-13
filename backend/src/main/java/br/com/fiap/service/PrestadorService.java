package br.com.fiap.service;

import br.com.fiap.dao.EmpresaDAO;
import br.com.fiap.dao.PrestadorDAO;
import br.com.fiap.dto.CadastroPrestadorDto;
import br.com.fiap.dto.DetalhesPrestadorDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Prestador;
import br.com.fiap.util.Validador;

import java.util.List;

public class PrestadorService {

    private final PrestadorDAO prestadorDao = new PrestadorDAO();
    private final EmpresaDAO empresaDao = new EmpresaDAO();

    public DetalhesPrestadorDto cadastrar(CadastroPrestadorDto dto) {
        Prestador prestador = validar(dto);
        prestadorDao.inserir(prestador);
        return DetalhesPrestadorDto.de(prestador);
    }

    public List<DetalhesPrestadorDto> listar(Long idEmpresa) {
        return prestadorDao.listar(idEmpresa).stream().map(DetalhesPrestadorDto::de).toList();
    }

    public DetalhesPrestadorDto buscar(long id) {
        return DetalhesPrestadorDto.de(prestadorDao.buscarPorId(id));
    }

    public DetalhesPrestadorDto atualizar(long id, CadastroPrestadorDto dto) {
        prestadorDao.buscarPorId(id);
        Prestador prestador = validar(dto);
        prestador.setId(id);
        prestadorDao.atualizar(prestador);
        return DetalhesPrestadorDto.de(prestador);
    }

    /** Prestador com visitas registradas não pode ser excluído (FK -> 409). */
    public void remover(long id) {
        prestadorDao.excluir(id);
    }

    Prestador validar(CadastroPrestadorDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados do prestador.");
        }
        Prestador prestador = new Prestador();
        prestador.setNome(Validador.textoObrigatorio(dto.getNome(), "nome", "o nome", 3, 80));
        prestador.setCpf(Validador.cpf(dto.getCpf()));
        prestador.setFuncao(Validador.textoObrigatorio(dto.getFuncao(), "funcao", "a função", 3, 50));
        Validador.idObrigatorio(dto.getIdEmpresa(), "idEmpresa", "a empresa");
        prestador.setEmpresa(empresaDao.buscarPorId(dto.getIdEmpresa()));
        return prestador;
    }
}
