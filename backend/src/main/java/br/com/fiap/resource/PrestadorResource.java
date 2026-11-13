package br.com.fiap.resource;

import br.com.fiap.dto.CadastroPrestadorDto;
import br.com.fiap.dto.DetalhesPrestadorDto;
import br.com.fiap.service.PrestadorService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/** CRUD de prestadores: /prestadores (filtro opcional ?empresa={id}) */
@Path("/prestadores")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrestadorResource {

    private final PrestadorService service = new PrestadorService();

    @POST
    public Response cadastrar(CadastroPrestadorDto dto, @Context UriInfo uriInfo) {
        DetalhesPrestadorDto criado = service.cadastrar(dto);
        UriBuilder uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id()));
        return Response.created(uri.build()).entity(criado).build();
    }

    @GET
    public List<DetalhesPrestadorDto> listar(@QueryParam("empresa") Long idEmpresa) {
        return service.listar(idEmpresa);
    }

    @GET
    @Path("/{id}")
    public DetalhesPrestadorDto buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @PUT
    @Path("/{id}")
    public DetalhesPrestadorDto atualizar(@PathParam("id") long id, CadastroPrestadorDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
