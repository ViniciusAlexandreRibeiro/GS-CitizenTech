package br.com.fiap.resource;

import br.com.fiap.dto.CadastroVisitaDto;
import br.com.fiap.dto.ConferenciaVisitaDto;
import br.com.fiap.dto.DetalhesVisitaDto;
import br.com.fiap.dto.ResultadoConferenciaDto;
import br.com.fiap.service.VisitaService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/** Agenda de visitas e conferência do código na porta. */
@Path("/visitas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VisitaResource {

    private final VisitaService service = new VisitaService();

    @POST
    public Response agendar(CadastroVisitaDto dto, @Context UriInfo uriInfo) {
        DetalhesVisitaDto criada = service.agendar(dto);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(criada.id())).build())
                .entity(criada).build();
    }

    @GET
    public List<DetalhesVisitaDto> listar(@QueryParam("idoso") Long idIdoso, @QueryParam("status") String status) {
        return service.listar(idIdoso, status);
    }

    @GET
    @Path("/{id}")
    public DetalhesVisitaDto buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    /** Remarca ou cancela ("status": "CANCELADA"). */
    @PUT
    @Path("/{id}")
    public DetalhesVisitaDto atualizar(@PathParam("id") long id, CadastroVisitaDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") long id) {
        service.remover(id);
        return Response.noContent().build();
    }

    /** O idoso digita o código que a pessoa na porta informou. */
    @POST
    @Path("/conferir")
    public ResultadoConferenciaDto conferir(ConferenciaVisitaDto dto) {
        return service.conferir(dto);
    }
}
