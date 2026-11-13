package br.com.fiap.dto;

import br.com.fiap.models.Visita;

import java.time.LocalDateTime;

public record DetalhesVisitaDto(Long id, Long idIdoso, String nomeIdoso, String bairro, Long idPrestador,
                                String visitante, String funcao, String empresa, LocalDateTime dataAgendada,
                                String codigo, String agendadoPor, String status, String descricaoStatus) {

    public static DetalhesVisitaDto de(Visita v) {
        boolean temPrestador = v.getPrestador() != null;
        return new DetalhesVisitaDto(v.getId(), v.getIdoso().getId(), v.getIdoso().getNome(), v.getIdoso().getBairro(),
                temPrestador ? v.getPrestador().getId() : null,
                v.getNomeExibicao(),
                temPrestador ? v.getPrestador().getFuncao() : null,
                temPrestador ? v.getPrestador().getEmpresa().getNome() : null,
                v.getDataAgendada(), v.getCodigo(), v.getAgendadoPor().name(), v.getStatus().name(),
                v.getDescricaoStatus());
    }
}
