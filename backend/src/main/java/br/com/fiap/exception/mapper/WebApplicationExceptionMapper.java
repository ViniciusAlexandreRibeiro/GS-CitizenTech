package br.com.fiap.exception.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Erros do próprio Jersey (rota inexistente, método errado, JSON malformado...).
 * Mantém o status original para que um 404 não vire 500.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        String mensagem = switch (status) {
            case 404 -> "Endereço não encontrado na API.";
            case 405 -> "Este método HTTP não é aceito neste endereço.";
            case 415 -> "Envie os dados em JSON (Content-Type: application/json).";
            case 400 -> "Requisição inválida: confira o JSON enviado.";
            default -> e.getMessage();
        };
        return Erros.resposta(status, mensagem, null);
    }
}
