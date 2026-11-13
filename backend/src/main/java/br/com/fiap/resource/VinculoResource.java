package br.com.fiap.resource;

import br.com.fiap.dto.DetalhesVinculoDto;
import br.com.fiap.dto.VinculoDto;
import br.com.fiap.service.VinculoService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Vínculos entre idosos e guardiões (relacionamento N:N).
 * A chave é composta, por isso as rotas usam os dois ids: /vinculos/{idIdoso}/{idGuardiao}
 */
@Path("/vinculos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VinculoResource {

    private final VinculoService service = new VinculoService();

    @POST
    public Response vincular(VinculoDto dto) {
        DetalhesVinculoDto criado = service.vincular(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @GET
    public List<DetalhesVinculoDto> listar(@QueryParam("idoso") Long idIdoso, @QueryParam("guardiao") Long idGuardiao) {
        return service.listar(idIdoso, idGuardiao);
    }

    @GET
    @Path("/{idIdoso}/{idGuardiao}")
    public DetalhesVinculoDto buscar(@PathParam("idIdoso") long idIdoso, @PathParam("idGuardiao") long idGuardiao) {
        return service.buscar(idIdoso, idGuardiao);
    }

    @PUT
    @Path("/{idIdoso}/{idGuardiao}")
    public DetalhesVinculoDto atualizar(@PathParam("idIdoso") long idIdoso, @PathParam("idGuardiao") long idGuardiao,
                                        VinculoDto dto) {
        return service.atualizar(idIdoso, idGuardiao, dto == null ? new VinculoDto() : dto);
    }

    @DELETE
    @Path("/{idIdoso}/{idGuardiao}")
    public Response remover(@PathParam("idIdoso") long idIdoso, @PathParam("idGuardiao") long idGuardiao) {
        service.remover(idIdoso, idGuardiao);
        return Response.noContent().build();
    }
}
