package br.com.fiap.resource;

import br.com.fiap.dto.CadastroIdosoDto;
import br.com.fiap.dto.DetalhesIdosoDto;
import br.com.fiap.service.IdosoService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/** CRUD de idosos: /idosos */
@Path("/idosos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IdosoResource {

    private final IdosoService service = new IdosoService();

    @POST
    public Response cadastrar(CadastroIdosoDto dto, @Context UriInfo uriInfo) {
        DetalhesIdosoDto criado = service.cadastrar(dto);
        UriBuilder uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id()));
        return Response.created(uri.build()).entity(criado).build();
    }

    @GET
    public List<DetalhesIdosoDto> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public DetalhesIdosoDto buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @PUT
    @Path("/{id}")
    public DetalhesIdosoDto atualizar(@PathParam("id") long id, CadastroIdosoDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
