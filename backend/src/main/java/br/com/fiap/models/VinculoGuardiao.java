package br.com.fiap.models;

import java.time.LocalDate;

/** Associação N:N entre idoso e guardião (tabela T_VINCULO). */
public class VinculoGuardiao {

    public static final int PRIORIDADE_MINIMA = 1;
    public static final int PRIORIDADE_MAXIMA = 5;

    private Idoso idoso;
    private Guardiao guardiao;
    private String parentesco;
    private int prioridade;
    private LocalDate dataVinculo;

    public VinculoGuardiao() {
    }

    public VinculoGuardiao(Idoso idoso, Guardiao guardiao, String parentesco, int prioridade, LocalDate dataVinculo) {
        this.idoso = idoso;
        this.guardiao = guardiao;
        this.parentesco = parentesco;
        this.prioridade = prioridade;
        this.dataVinculo = dataVinculo;
    }

    public Idoso getIdoso() {
        return idoso;
    }

    public void setIdoso(Idoso idoso) {
        this.idoso = idoso;
    }

    public Guardiao getGuardiao() {
        return guardiao;
    }

    public void setGuardiao(Guardiao guardiao) {
        this.guardiao = guardiao;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDate getDataVinculo() {
        return dataVinculo;
    }

    public void setDataVinculo(LocalDate dataVinculo) {
        this.dataVinculo = dataVinculo;
    }
}
