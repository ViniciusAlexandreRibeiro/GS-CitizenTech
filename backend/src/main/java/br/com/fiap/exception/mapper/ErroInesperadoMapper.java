package br.com.fiap.exception.mapper;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.format.DateTimeParseException;

/** Última barreira: qualquer erro não previsto vira uma resposta JSON e a API continua no ar. */
@Provider
public class ErroInesperadoMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable e) {
        if (temCausa(e, JsonbException.class) || temCausa(e, DateTimeParseException.class)) {
            return Erros.resposta(400, "JSON inválido: confira os campos e o formato das datas (AAAA-MM-DDTHH:MM).", null);
        }
        if (e instanceof IllegalStateException) {
            return Erros.resposta(409, e.getMessage(), null);
        }
        System.err.println("[erro inesperado] " + e);
        e.printStackTrace();
        return Erros.resposta(500, "Ocorreu um erro inesperado. Tente novamente.", null);
    }

    private boolean temCausa(Throwable e, Class<? extends Throwable> tipo) {
        for (Throwable atual = e; atual != null; atual = atual.getCause()) {
            if (tipo.isInstance(atual)) {
                return true;
            }
        }
        return false;
    }
}
