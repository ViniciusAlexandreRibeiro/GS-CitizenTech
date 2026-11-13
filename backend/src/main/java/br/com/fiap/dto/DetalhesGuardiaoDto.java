package br.com.fiap.dto;

import br.com.fiap.models.Guardiao;

public record DetalhesGuardiaoDto(Long id, String nome, String cpfMascarado, String telefone, String email) {

    public static DetalhesGuardiaoDto de(Guardiao g) {
        return new DetalhesGuardiaoDto(g.getId(), g.getNome(), g.getCpfMascarado(), g.getTelefone(), g.getEmail());
    }
}
