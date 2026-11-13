package br.com.fiap.resource;

import br.com.fiap.dto.BloqueiosContatoDto;
import br.com.fiap.dto.CadastroSolicitacaoDto;
import br.com.fiap.dto.DetalhesSolicitacaoDto;
import br.com.fiap.dto.RespostaSolicitacaoDto;
import br.com.fiap.service.SolicitacaoService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

/** Pedidos de ajuda do idoso e respostas dos guardiões. */
@Path("/solicitacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SolicitacaoResource {

    private final SolicitacaoService service = new SolicitacaoService();

    @POST
    public Response abrir(CadastroSolicitacaoDto dto, @Context UriInfo uriInfo) {
        DetalhesSolicitacaoDto criada = service.abrir(dto);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(criada.id())).build())
                .entity(criada).build();
    }

    /** Filtros opcionais: ?idoso={id}&guardiao={id}&status=PENDENTE */
    @GET
    public List<DetalhesSolicitacaoDto> listar(@QueryParam("idoso") Long idIdoso,
                                               @QueryParam("guardiao") Long idGuardiao,
                                               @QueryParam("status") String status) {
        return service.listar(idIdoso, idGuardiao, status);
    }

    /** Quantas vezes um contato já foi bloqueado: /solicitacoes/bloqueios?contato=11988771234 */
    @GET
    @Path("/bloqueios")
    public BloqueiosContatoDto bloqueios(@QueryParam("contato") String contato) {
        return service.bloqueiosDoContato(contato);
    }

    @GET
    @Path("/{id}")
    public DetalhesSolicitacaoDto buscar(@PathParam("id") long id) {
        return service.buscar(id);
    }

    @PUT
    @Path("/{id}")
    public DetalhesSolicitacaoDto atualizar(@PathParam("id") long id, CadastroSolicitacaoDto dto) {
        return service.atualizar(id, dto);
    }

    /** O guardião libera ou bloqueia: {"idGuardiao": 1, "decisao": "BLOQUEAR", "orientacao": "..."} */
    @PATCH
    @Path("/{id}/resposta")
    public DetalhesSolicitacaoDto responder(@PathParam("id") long id, RespostaSolicitacaoDto dto) {
        return service.responder(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
