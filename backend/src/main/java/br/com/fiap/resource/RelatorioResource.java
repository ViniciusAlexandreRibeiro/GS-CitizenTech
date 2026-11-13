package br.com.fiap.resource;

import br.com.fiap.dto.relatorio.ContatoRecorrenteDto;
import br.com.fiap.dto.relatorio.IdosoSemGuardiaoDto;
import br.com.fiap.dto.relatorio.IndicadoresDto;
import br.com.fiap.dto.relatorio.SolicitacoesPorTipoDto;
import br.com.fiap.dto.relatorio.TempoRespostaDto;
import br.com.fiap.dto.relatorio.TentativasBairroDto;
import br.com.fiap.dto.relatorio.ValorProtegidoDto;
import br.com.fiap.service.RelatorioService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/** Relatórios da página de impacto (consultas com JOIN e agregação). */
@Path("/relatorios")
@Produces(MediaType.APPLICATION_JSON)
public class RelatorioResource {

    private final RelatorioService service = new RelatorioService();

    @GET
    @Path("/indicadores")
    public IndicadoresDto indicadores() {
        return service.indicadores();
    }

    @GET
    @Path("/valor-protegido")
    public List<ValorProtegidoDto> valorProtegido() {
        return service.valorProtegido();
    }

    @GET
    @Path("/porta-por-bairro")
    public List<TentativasBairroDto> portaPorBairro() {
        return service.portaPorBairro();
    }

    @GET
    @Path("/por-tipo")
    public List<SolicitacoesPorTipoDto> porTipo() {
        return service.porTipo();
    }

    @GET
    @Path("/tempo-resposta")
    public List<TempoRespostaDto> tempoResposta() {
        return service.tempoResposta();
    }

    @GET
    @Path("/contatos-recorrentes")
    public List<ContatoRecorrenteDto> contatosRecorrentes() {
        return service.contatosRecorrentes();
    }

    @GET
    @Path("/idosos-sem-guardiao")
    public List<IdosoSemGuardiaoDto> idososSemGuardiao() {
        return service.idososSemGuardiao();
    }
}
