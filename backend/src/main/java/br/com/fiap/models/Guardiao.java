package br.com.fiap.models;

/** Familiar ou pessoa de confiança que responde aos pedidos de ajuda. */
public class Guardiao extends Pessoa {

    private String email;

    public Guardiao() {
    }

    public Guardiao(Long id, String nome, String cpf, String telefone, String email) {
        super(id, nome, cpf, telefone);
        this.email = email;
    }

    @Override
    public String getPapel() {
        return "Guardião";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
