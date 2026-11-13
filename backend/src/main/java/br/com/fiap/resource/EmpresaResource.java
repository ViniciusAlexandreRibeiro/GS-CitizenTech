package br.com.fiap.resource;

import br.com.fiap.dto.CadastroEmpresaDto;
import br.com.fiap.dto.DetalhesEmpresaDto;
import br.com.fiap.service.EmpresaService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/** CRUD de empresas: /empresas */
@Path("/empresas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmpresaResource {

    private final EmpresaService service = new EmpresaService();

    @POST
    public Response cadastrar(CadastroEmpresaDto dto, @Context UriInfo uriInfo) {
        DetalhesEmpresaDto criado = service.cadastrar(dto);
        UriBuilder uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id()));
        return Response.created(uri.build()).entity(criado).build();
    }

    @GET
    public List<DetalhesEmpresaDto> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public DetalhesEmpresaDto buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @PUT
    @Path("/{id}")
    public DetalhesEmpresaDto atualizar(@PathParam("id") long id, CadastroEmpresaDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
