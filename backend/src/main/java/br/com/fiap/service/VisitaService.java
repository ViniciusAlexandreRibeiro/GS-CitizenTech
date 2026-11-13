package br.com.fiap.service;

import br.com.fiap.dao.IdosoDAO;
import br.com.fiap.dao.PrestadorDAO;
import br.com.fiap.dao.SolicitacaoDAO;
import br.com.fiap.dao.VinculoDAO;
import br.com.fiap.dao.VisitaDAO;
import br.com.fiap.dto.CadastroVisitaDto;
import br.com.fiap.dto.ConferenciaVisitaDto;
import br.com.fiap.dto.DetalhesVisitaDto;
import br.com.fiap.dto.ResultadoConferenciaDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Idoso;
import br.com.fiap.models.SolicitacaoVerificacao;
import br.com.fiap.models.Visita;
import br.com.fiap.models.enums.AgendadoPor;
import br.com.fiap.models.enums.StatusVisita;
import br.com.fiap.models.enums.TipoSolicitacao;
import br.com.fiap.util.Documentos;
import br.com.fiap.util.Validador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VisitaService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");
    private static final int TENTATIVAS_CODIGO = 20;

    private final VisitaDAO visitaDao = new VisitaDAO();
    private final IdosoDAO idosoDao = new IdosoDAO();
    private final PrestadorDAO prestadorDao = new PrestadorDAO();
    private final VinculoDAO vinculoDao = new VinculoDAO();
    private final SolicitacaoDAO solicitacaoDao = new SolicitacaoDAO();

    public DetalhesVisitaDto agendar(CadastroVisitaDto dto) {
        Visita visita = new Visita();
        preencher(visita, dto);
        visita.setStatus(StatusVisita.AGENDADA);
        visita.setCodigo(gerarCodigoUnico());
        visitaDao.inserir(visita);
        return DetalhesVisitaDto.de(visitaDao.buscarPorId(visita.getId()));
    }

    public List<DetalhesVisitaDto> listar(Long idIdoso, String status) {
        StatusVisita filtro = status == null || status.isBlank() ? null
                : Validador.opcao(StatusVisita.class, status, "status", "o status");
        return visitaDao.listar(idIdoso, filtro).stream().map(DetalhesVisitaDto::de).toList();
    }

    public DetalhesVisitaDto buscar(long id) {
        return DetalhesVisitaDto.de(visitaDao.buscarPorId(id));
    }

    /** Remarca (status AGENDADA) ou cancela (status CANCELADA) uma visita que ainda não aconteceu. */
    public DetalhesVisitaDto atualizar(long id, CadastroVisitaDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados da visita.");
        }
        Visita visita = visitaDao.buscarPorId(id);
        if (visita.getStatus() == StatusVisita.CONFIRMADA) {
            throw new RegraNegocioException(RegraNegocioException.CONFLITO,
                    "Esta visita já foi confirmada na porta e não pode ser alterada.", null);
        }
        StatusVisita novoStatus = dto.getStatus() == null || dto.getStatus().isBlank() ? StatusVisita.AGENDADA
                : Validador.opcao(StatusVisita.class, dto.getStatus(), "status", "o status");

        if (novoStatus == StatusVisita.CONFIRMADA) {
            throw new RegraNegocioException("A visita só é confirmada pelo idoso, conferindo o código na porta.", "status");
        }
        if (novoStatus == StatusVisita.CANCELADA) {
            visita.cancelar();
        } else {
            preencher(visita, dto);
            visita.setStatus(StatusVisita.AGENDADA);
        }
        visitaDao.atualizar(visita);
        return DetalhesVisitaDto.de(visitaDao.buscarPorId(id));
    }

    public void remover(long id) {
        visitaDao.excluir(id);
    }

    /**
     * O idoso digita o código que a pessoa na porta informou.
     * Válido: a visita é confirmada. Qualquer problema: "não abra" e um alerta vai para os guardiões.
     */
    public ResultadoConferenciaDto conferir(ConferenciaVisitaDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie o idoso e o código da visita.");
        }
        Validador.idObrigatorio(dto.getIdIdoso(), "idIdoso", "quem está usando o sistema");
        Idoso idoso = idosoDao.buscarPorId(dto.getIdIdoso());
        String codigo = Documentos.somenteDigitos(dto.getCodigo());
        LocalDateTime agora = LocalDateTime.now();

        if (codigo == null || codigo.isEmpty()) {
            return negar(idoso, null, null, "A pessoa disse que não tem código de visita.", agora);
        }
        if (codigo.length() != 6) {
            throw new RegraNegocioException("O código tem exatamente 6 números.", "codigo");
        }

        Visita visita = visitaDao.buscarPorCodigo(codigo);
        if (visita == null) {
            return negar(idoso, null, codigo, "Nenhuma visita foi agendada com este código.", agora);
        }
        if (!visita.getIdoso().getId().equals(idoso.getId())) {
            return negar(idoso, null, codigo, "Este código não pertence a uma visita sua.", agora);
        }
        if (visita.getStatus() == StatusVisita.CANCELADA) {
            return negar(idoso, visita, codigo, "Esta visita foi cancelada.", agora);
        }
        if (visita.getStatus() == StatusVisita.CONFIRMADA) {
            return negar(idoso, visita, codigo, "Este código já foi usado.", agora);
        }
        if (!visita.estaNaJanela(agora)) {
            return negar(idoso, visita, codigo,
                    "Fora do horário: a visita está marcada para " + visita.getDataAgendada().format(FORMATO) + ".", agora);
        }

        visita.confirmar();
        visitaDao.atualizar(visita);
        return ResultadoConferenciaDto.confirmada(DetalhesVisitaDto.de(visitaDao.buscarPorId(visita.getId())));
    }

    private ResultadoConferenciaDto negar(Idoso idoso, Visita visita, String codigo, String motivo, LocalDateTime agora) {
        SolicitacaoVerificacao alerta = new SolicitacaoVerificacao();
        alerta.setIdoso(idoso);
        alerta.setVisita(visita);
        alerta.setTipo(TipoSolicitacao.VISITA);
        alerta.setDescricao("Conferência na porta: " + motivo + (codigo == null ? "" : " Código digitado: " + codigo + "."));
        alerta.setDataAbertura(agora);
        solicitacaoDao.inserir(alerta);

        List<String> guardioes = vinculoDao.listar(idoso.getId(), null).stream()
                .map(v -> v.getGuardiao().getNome())
                .toList();
        String complemento = guardioes.isEmpty()
                ? motivo + " Você ainda não tem guardiões cadastrados: ligue para alguém de confiança."
                : motivo;
        return ResultadoConferenciaDto.negada(complemento, alerta.getId(), guardioes);
    }

    private void preencher(Visita visita, CadastroVisitaDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados da visita.");
        }
        Validador.idObrigatorio(dto.getIdIdoso(), "idIdoso", "o idoso");
        visita.setIdoso(idosoDao.buscarPorId(dto.getIdIdoso()));

        AgendadoPor agendadoPor = Validador.opcao(AgendadoPor.class, dto.getAgendadoPor(), "agendadoPor", "quem está agendando");
        visita.setAgendadoPor(agendadoPor);

        if (dto.getIdPrestador() != null) {
            visita.setPrestador(prestadorDao.buscarPorId(dto.getIdPrestador()));
            visita.setNomeVisitante(null);
        } else if (agendadoPor == AgendadoPor.EMPRESA) {
            throw new RegraNegocioException("Visitas agendadas por empresa precisam de um prestador.", "idPrestador");
        } else {
            visita.setPrestador(null);
            visita.setNomeVisitante(Validador.textoObrigatorio(dto.getNomeVisitante(), "nomeVisitante",
                    "o nome de quem vai visitar", 3, 80));
        }

        LocalDateTime data = dto.getDataAgendada();
        if (data == null) {
            throw new RegraNegocioException("Informe a data e a hora da visita.", "dataAgendada");
        }
        LocalDateTime agora = LocalDateTime.now();
        if (!data.isAfter(agora)) {
            throw new RegraNegocioException("A visita precisa ser agendada para um horário futuro.", "dataAgendada");
        }
        if (data.isAfter(agora.plusYears(1))) {
            throw new RegraNegocioException("Agende com no máximo um ano de antecedência.", "dataAgendada");
        }
        visita.setDataAgendada(data.withSecond(0).withNano(0));
    }

    private String gerarCodigoUnico() {
        for (int i = 0; i < TENTATIVAS_CODIGO; i++) {
            String codigo = Visita.gerarCodigo();
            if (visitaDao.buscarPorCodigo(codigo) == null) {
                return codigo;
            }
        }
        throw new RegraNegocioException(RegraNegocioException.CONFLITO,
                "Não foi possível gerar um código livre. Tente novamente.", null);
    }
}
