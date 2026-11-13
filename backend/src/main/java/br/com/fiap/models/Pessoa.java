package br.com.fiap.models;

import br.com.fiap.util.Documentos;

/** Dados comuns a todas as pessoas do sistema. */
public abstract class Pessoa {

    private Long id;
    private String nome;
    private String cpf;
    private String telefone;

    protected Pessoa() {
    }

    protected Pessoa(Long id, String nome, String cpf, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    /** Papel da pessoa na rede (Idoso, Guardião ou Prestador). */
    public abstract String getPapel();

    /** CPF com o início e o fim escondidos, para exibir em tela. */
    public String getCpfMascarado() {
        return Documentos.mascararCpf(cpf);
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
