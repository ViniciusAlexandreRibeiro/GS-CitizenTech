package br.com.fiap.dto;

/** Dados enviados para cadastrar ou atualizar uma empresa. */
public class CadastroEmpresaDto {

    private String nome;
    private String cnpj;
    private String setor;

    public CadastroEmpresaDto() {
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

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }
}
