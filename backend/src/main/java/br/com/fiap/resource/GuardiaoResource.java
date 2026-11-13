package br.com.fiap.resource;

import br.com.fiap.dto.CadastroGuardiaoDto;
import br.com.fiap.dto.DetalhesGuardiaoDto;
import br.com.fiap.service.GuardiaoService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/** CRUD de guardiões: /guardioes */
@Path("/guardioes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GuardiaoResource {

    private final GuardiaoService service = new GuardiaoService();

    @POST
    public Response cadastrar(CadastroGuardiaoDto dto, @Context UriInfo uriInfo) {
        DetalhesGuardiaoDto criado = service.cadastrar(dto);
        UriBuilder uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id()));
        return Response.created(uri.build()).entity(criado).build();
    }

    @GET
    public List<DetalhesGuardiaoDto> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public DetalhesGuardiaoDto buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @PUT
    @Path("/{id}")
    public DetalhesGuardiaoDto atualizar(@PathParam("id") long id, CadastroGuardiaoDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
