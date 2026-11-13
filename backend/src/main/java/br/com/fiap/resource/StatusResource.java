package br.com.fiap.resource;

import br.com.fiap.exception.GuardiaoException;
import br.com.fiap.db.ConnectionFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Rotas de verificação: a web usa /status para saber se a API e o banco estão no ar. */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {

    @GET
    public Map<String, String> inicio() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("api", "Guardião");
        info.put("versao", "1.0.0");
        info.put("status", "/status");
        return info;
    }

    @GET
    @Path("/status")
    public Map<String, String> status() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("api", "ok");
        try (Connection con = ConnectionFactory.getConnection()) {
            status.put("banco", con.isValid(5) ? "ok" : "indisponivel");
        } catch (GuardiaoException | SQLException e) {
            status.put("banco", "indisponivel");
            status.put("mensagem", e.getMessage());
        }
        return status;
    }
}
