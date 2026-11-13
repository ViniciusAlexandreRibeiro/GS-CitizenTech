package br.com.fiap.models;

/** Profissional de uma empresa que faz visitas na casa do idoso. */
public class Prestador extends Pessoa {

    private Empresa empresa;
    private String funcao;

    public Prestador() {
    }

    public Prestador(Long id, String nome, String cpf, String telefone, Empresa empresa, String funcao) {
        super(id, nome, cpf, telefone);
        this.empresa = empresa;
        this.funcao = funcao;
    }

    @Override
    public String getPapel() {
        return "Prestador";
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
}
