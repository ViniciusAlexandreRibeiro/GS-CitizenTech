package br.com.fiap.service;

import br.com.fiap.dao.RelatorioDAO;
import br.com.fiap.dto.relatorio.ContatoRecorrenteDto;
import br.com.fiap.dto.relatorio.IdosoSemGuardiaoDto;
import br.com.fiap.dto.relatorio.IndicadoresDto;
import br.com.fiap.dto.relatorio.SolicitacoesPorTipoDto;
import br.com.fiap.dto.relatorio.TempoRespostaDto;
import br.com.fiap.dto.relatorio.TentativasBairroDto;
import br.com.fiap.dto.relatorio.ValorProtegidoDto;

import java.util.List;

public class RelatorioService {

    private final RelatorioDAO relatorioDao = new RelatorioDAO();

    public IndicadoresDto indicadores() {
        return relatorioDao.indicadores();
    }

    public List<ValorProtegidoDto> valorProtegido() {
        return relatorioDao.valorProtegidoPorIdoso();
    }

    public List<TentativasBairroDto> portaPorBairro() {
        return relatorioDao.tentativasNaPortaPorBairro();
    }

    public List<SolicitacoesPorTipoDto> porTipo() {
        return relatorioDao.solicitacoesPorTipo();
    }

    public List<TempoRespostaDto> tempoResposta() {
        return relatorioDao.tempoMedioDeResposta();
    }

    public List<ContatoRecorrenteDto> contatosRecorrentes() {
        return relatorioDao.contatosRecorrentes();
    }

    public List<IdosoSemGuardiaoDto> idososSemGuardiao() {
        return relatorioDao.idososSemGuardiao();
    }
}
