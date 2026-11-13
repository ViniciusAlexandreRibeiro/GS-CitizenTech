package br.com.fiap.exception;

/**
 * Entrada inválida ou regra de negócio violada.
 * 400 = dado inválido, 403 = ação não permitida, 409 = conflito com o que já existe.
 */
public class RegraNegocioException extends GuardiaoException {

    public static final int DADO_INVALIDO = 400;
    public static final int NAO_PERMITIDO = 403;
    public static final int CONFLITO = 409;

    public RegraNegocioException(String mensagem) {
        this(DADO_INVALIDO, mensagem, null);
    }

    public RegraNegocioException(String mensagem, String campo) {
        this(DADO_INVALIDO, mensagem, campo);
    }

    public RegraNegocioException(int status, String mensagem, String campo) {
        super(status, mensagem, campo, null);
    }
}
