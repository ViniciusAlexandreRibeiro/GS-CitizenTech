package br.com.fiap.dto;

/** Dados para vincular (ou atualizar o vínculo de) um guardião a um idoso. */
public class VinculoDto {

    private Long idIdoso;
    private Long idGuardiao;
    private String parentesco;
    private Integer prioridade;

    public VinculoDto() {
    }

    public Long getIdIdoso() {
        return idIdoso;
    }

    public void setIdIdoso(Long idIdoso) {
        this.idIdoso = idIdoso;
    }

    public Long getIdGuardiao() {
        return idGuardiao;
    }

    public void setIdGuardiao(Long idGuardiao) {
        this.idGuardiao = idGuardiao;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public Integer getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }
}
