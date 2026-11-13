package br.com.fiap.exception;

/** Registro procurado pelo id (ou código) não existe no banco. HTTP 404. */
public class EntidadeNaoEncontradaException extends GuardiaoException {

    public EntidadeNaoEncontradaException(String mensagem) {
        super(404, mensagem, null, null);
    }
}
