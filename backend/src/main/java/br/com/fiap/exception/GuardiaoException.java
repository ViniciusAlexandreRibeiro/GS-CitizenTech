package br.com.fiap.exception;

/**
 * Base de todos os erros "esperados" do sistema. Carrega o status HTTP e,
 * quando houver, o campo do formulário que causou o problema, para que a
 * interface mostre a mensagem ao lado do campo certo.
 */
public abstract class GuardiaoException extends RuntimeException {

    private final int status;
    private final String campo;

    protected GuardiaoException(int status, String mensagem, String campo, Throwable causa) {
        super(mensagem, causa);
        this.status = status;
        this.campo = campo;
    }

    public int getStatus() {
        return status;
    }

    public String getCampo() {
        return campo;
    }
}
