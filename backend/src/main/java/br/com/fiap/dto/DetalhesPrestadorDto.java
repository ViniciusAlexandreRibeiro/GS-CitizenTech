package br.com.fiap.dto;

import br.com.fiap.models.Prestador;

public record DetalhesPrestadorDto(Long id, String nome, String cpfMascarado, String funcao,
                                   Long idEmpresa, String nomeEmpresa) {

    public static DetalhesPrestadorDto de(Prestador p) {
        return new DetalhesPrestadorDto(p.getId(), p.getNome(), p.getCpfMascarado(), p.getFuncao(),
                p.getEmpresa().getId(), p.getEmpresa().getNome());
    }
}
