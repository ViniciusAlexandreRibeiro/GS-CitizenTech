package br.com.fiap.dto;

/** Decisão do guardião: LIBERAR ou BLOQUEAR, com uma orientação para o idoso. */
public class RespostaSolicitacaoDto {

    private Long idGuardiao;
    private String decisao;
    private String orientacao;

    public RespostaSolicitacaoDto() {
    }

    public Long getIdGuardiao() {
        return idGuardiao;
    }

    public void setIdGuardiao(Long idGuardiao) {
        this.idGuardiao = idGuardiao;
    }

    public String getDecisao() {
        return decisao;
    }

    public void setDecisao(String decisao) {
        this.decisao = decisao;
    }

    public String getOrientacao() {
        return orientacao;
    }

    public void setOrientacao(String orientacao) {
        this.orientacao = orientacao;
    }
}
