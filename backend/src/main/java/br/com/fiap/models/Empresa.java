package br.com.fiap.models;

import br.com.fiap.models.enums.SetorEmpresa;

/** Empresa que realiza visitas domiciliares (energia, água, internet...). */
public class Empresa {

    private Long id;
    private String nome;
    private String cnpj;
    private SetorEmpresa setor;

    public Empresa() {
    }

    public Empresa(Long id, String nome, String cnpj, SetorEmpresa setor) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.setor = setor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public SetorEmpresa getSetor() {
        return setor;
    }

    public void setSetor(SetorEmpresa setor) {
        this.setor = setor;
    }
}
