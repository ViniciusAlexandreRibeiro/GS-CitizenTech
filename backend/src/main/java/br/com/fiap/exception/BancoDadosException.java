package br.com.fiap.exception;

/** Banco fora do ar, credencial errada ou erro inesperado de SQL. HTTP 503. */
public class BancoDadosException extends GuardiaoException {

    public BancoDadosException(String mensagem, Throwable causa) {
        super(503, mensagem, null, causa);
    }
}
