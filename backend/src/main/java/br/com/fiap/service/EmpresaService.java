package br.com.fiap.service;

import br.com.fiap.dao.EmpresaDAO;
import br.com.fiap.dto.CadastroEmpresaDto;
import br.com.fiap.dto.DetalhesEmpresaDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Empresa;
import br.com.fiap.models.enums.SetorEmpresa;
import br.com.fiap.util.Validador;

import java.util.List;

public class EmpresaService {

    private final EmpresaDAO empresaDao = new EmpresaDAO();

    public DetalhesEmpresaDto cadastrar(CadastroEmpresaDto dto) {
        Empresa empresa = validar(dto);
        empresaDao.inserir(empresa);
        return DetalhesEmpresaDto.de(empresa);
    }

    public List<DetalhesEmpresaDto> listar() {
        return empresaDao.listarTodos().stream().map(DetalhesEmpresaDto::de).toList();
    }

    public DetalhesEmpresaDto buscar(long id) {
        return DetalhesEmpresaDto.de(empresaDao.buscarPorId(id));
    }

    public DetalhesEmpresaDto atualizar(long id, CadastroEmpresaDto dto) {
        empresaDao.buscarPorId(id);
        Empresa empresa = validar(dto);
        empresa.setId(id);
        empresaDao.atualizar(empresa);
        return DetalhesEmpresaDto.de(empresa);
    }

    /** Empresa com prestadores cadastrados não pode ser excluída (FK -> 409). */
    public void remover(long id) {
        empresaDao.excluir(id);
    }

    Empresa validar(CadastroEmpresaDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados da empresa.");
        }
        Empresa empresa = new Empresa();
        empresa.setNome(Validador.textoObrigatorio(dto.getNome(), "nome", "o nome da empresa", 2, 80));
        empresa.setCnpj(Validador.cnpj(dto.getCnpj()));
        empresa.setSetor(Validador.opcao(SetorEmpresa.class, dto.getSetor(), "setor", "o setor"));
        return empresa;
    }
}
