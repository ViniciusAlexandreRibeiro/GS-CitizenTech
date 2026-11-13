package br.com.fiap.dto;

import br.com.fiap.models.SolicitacaoVerificacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalhesSolicitacaoDto(Long id, Long idIdoso, String nomeIdoso, String bairro, String tipo,
                                     String descricaoTipo, String descricao, BigDecimal valor, String contatoSuspeito,
                                     String status, String descricaoStatus, String orientacao,
                                     Long idGuardiaoResposta, String nomeGuardiaoResposta,
                                     LocalDateTime dataAbertura, LocalDateTime dataResposta,
                                     BigDecimal limitePix, boolean aprovacaoObrigatoria, Long idVisita,
                                     Integer bloqueiosDoContato) {

    public static DetalhesSolicitacaoDto de(SolicitacaoVerificacao s) {
        return de(s, null);
    }

    public static DetalhesSolicitacaoDto de(SolicitacaoVerificacao s, Integer bloqueiosDoContato) {
        return new DetalhesSolicitacaoDto(s.getId(), s.getIdoso().getId(), s.getIdoso().getNome(),
                s.getIdoso().getBairro(), s.getTipo().name(), s.getTipo().getDescricao(), s.getDescricao(),
                s.getValor(), s.getContatoSuspeito(), s.getStatus().name(), s.getDescricaoStatus(), s.getOrientacao(),
                s.getGuardiaoResposta() == null ? null : s.getGuardiaoResposta().getId(),
                s.getGuardiaoResposta() == null ? null : s.getGuardiaoResposta().getNome(),
                s.getDataAbertura(), s.getDataResposta(), s.getIdoso().getLimitePix(),
                s.exigeAprovacaoObrigatoria(), s.getVisita() == null ? null : s.getVisita().getId(),
                bloqueiosDoContato);
    }
}
