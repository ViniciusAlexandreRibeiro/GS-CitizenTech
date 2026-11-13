package br.com.fiap.dto;

import br.com.fiap.models.Idoso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalhesIdosoDto(Long id, String nome, String cpfMascarado, String telefone, String bairro,
                               LocalDate dataNascimento, int idade, BigDecimal limitePix, int qtGuardioes) {

    public static DetalhesIdosoDto de(Idoso i, int qtGuardioes) {
        return new DetalhesIdosoDto(i.getId(), i.getNome(), i.getCpfMascarado(), i.getTelefone(), i.getBairro(),
                i.getDataNascimento(), i.getIdade(), i.getLimitePix(), qtGuardioes);
    }
}
