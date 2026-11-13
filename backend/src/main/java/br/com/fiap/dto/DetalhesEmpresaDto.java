package br.com.fiap.dto;

import br.com.fiap.models.Empresa;
import br.com.fiap.util.Documentos;

public record DetalhesEmpresaDto(Long id, String nome, String cnpj, String setor) {

    public static DetalhesEmpresaDto de(Empresa e) {
        return new DetalhesEmpresaDto(e.getId(), e.getNome(), Documentos.formatarCnpj(e.getCnpj()), e.getSetor().name());
    }
}
