package br.com.fiap.exception.mapper;

import br.com.fiap.exception.BancoDadosException;
import br.com.fiap.exception.GuardiaoException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Erros esperados (404, 400, 403, 409, 503) viram JSON com mensagem clara. */
@Provider
public class GuardiaoExceptionMapper implements ExceptionMapper<GuardiaoException> {

    @Override
    public Response toResponse(GuardiaoException e) {
        if (e instanceof BancoDadosException && e.getCause() != null) {
            System.err.println("[banco] " + e.getMessage() + " -> " + e.getCause().getMessage());
        }
        return Erros.resposta(e.getStatus(), e.getMessage(), e.getCampo());
    }
}
