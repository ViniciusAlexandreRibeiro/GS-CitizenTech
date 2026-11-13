package br.com.fiap.exception.mapper;

import br.com.fiap.dto.ErroDto;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Monta a resposta de erro no formato único da API. */
final class Erros {

    private Erros() {
    }

    static Response resposta(int status, String mensagem, String campo) {
        ErroDto erro = new ErroDto(status, titulo(status), mensagem, campo);
        return Response.status(status).entity(erro).type(MediaType.APPLICATION_JSON).build();
    }

    private static String titulo(int status) {
        return switch (status) {
            case 400 -> "Dados inválidos";
            case 403 -> "Não permitido";
            case 404 -> "Não encontrado";
            case 405 -> "Método não permitido";
            case 409 -> "Conflito";
            case 415 -> "Formato não suportado";
            case 503 -> "Banco de dados indisponível";
            default -> status >= 500 ? "Erro interno" : "Erro na requisição";
        };
    }
}
