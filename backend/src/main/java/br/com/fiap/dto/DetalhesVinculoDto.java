package br.com.fiap.dto;

import br.com.fiap.models.VinculoGuardiao;

import java.time.LocalDate;

public record DetalhesVinculoDto(Long idIdoso, String nomeIdoso, Long idGuardiao, String nomeGuardiao,
                                 String telefoneGuardiao, String parentesco, int prioridade, LocalDate dataVinculo) {

    public static DetalhesVinculoDto de(VinculoGuardiao v) {
        return new DetalhesVinculoDto(v.getIdoso().getId(), v.getIdoso().getNome(), v.getGuardiao().getId(),
                v.getGuardiao().getNome(), v.getGuardiao().getTelefone(), v.getParentesco(), v.getPrioridade(),
                v.getDataVinculo());
    }
}
