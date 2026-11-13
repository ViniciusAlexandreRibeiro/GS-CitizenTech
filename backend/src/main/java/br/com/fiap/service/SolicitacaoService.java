package br.com.fiap.service;

import br.com.fiap.dao.GuardiaoDAO;
import br.com.fiap.dao.IdosoDAO;
import br.com.fiap.dao.SolicitacaoDAO;
import br.com.fiap.dao.VinculoDAO;
import br.com.fiap.dto.BloqueiosContatoDto;
import br.com.fiap.dto.CadastroSolicitacaoDto;
import br.com.fiap.dto.DetalhesSolicitacaoDto;
import br.com.fiap.dto.RespostaSolicitacaoDto;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.models.Guardiao;
import br.com.fiap.models.SolicitacaoVerificacao;
import br.com.fiap.models.enums.DecisaoGuardiao;
import br.com.fiap.models.enums.StatusSolicitacao;
import br.com.fiap.models.enums.TipoSolicitacao;
import br.com.fiap.util.Contatos;
import br.com.fiap.util.Validador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Pedidos de ajuda do idoso e as respostas dos guardiões. */
public class SolicitacaoService {

    private final SolicitacaoDAO solicitacaoDao = new SolicitacaoDAO();
    private final IdosoDAO idosoDao = new IdosoDAO();
    private final GuardiaoDAO guardiaoDao = new GuardiaoDAO();
    private final VinculoDAO vinculoDao = new VinculoDAO();

    public DetalhesSolicitacaoDto abrir(CadastroSolicitacaoDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados do pedido de ajuda.");
        }
        Validador.idObrigatorio(dto.getIdIdoso(), "idIdoso", "quem está pedindo ajuda");
        SolicitacaoVerificacao s = new SolicitacaoVerificacao();
        s.setIdoso(idosoDao.buscarPorId(dto.getIdIdoso()));
        preencherDados(s, dto);

        if (vinculoDao.contarGuardioesDoIdoso(s.getIdoso().getId()) == 0) {
            throw new RegraNegocioException(RegraNegocioException.CONFLITO,
                    "Cadastre pelo menos um guardião para este idoso antes de pedir ajuda.", "idIdoso");
        }
        s.setStatus(StatusSolicitacao.PENDENTE);
        s.setDataAbertura(LocalDateTime.now());
        solicitacaoDao.inserir(s);
        return detalhar(solicitacaoDao.buscarPorId(s.getId()));
    }

    public List<DetalhesSolicitacaoDto> listar(Long idIdoso, Long idGuardiao, String status) {
        StatusSolicitacao filtro = status == null || status.isBlank() ? null
                : Validador.opcao(StatusSolicitacao.class, status, "status", "o status");
        return solicitacaoDao.listar(idIdoso, idGuardiao, filtro).stream().map(DetalhesSolicitacaoDto::de).toList();
    }

    public DetalhesSolicitacaoDto buscar(long id) {
        return detalhar(solicitacaoDao.buscarPorId(id));
    }

    /** O idoso pode corrigir o pedido enquanto nenhum guardião respondeu. */
    public DetalhesSolicitacaoDto atualizar(long id, CadastroSolicitacaoDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie os dados do pedido de ajuda.");
        }
        SolicitacaoVerificacao s = solicitacaoDao.buscarPorId(id);
        if (!s.estaPendente()) {
            throw new RegraNegocioException(RegraNegocioException.CONFLITO,
                    "Esta solicitação já foi respondida e não pode ser alterada.", null);
        }
        preencherDados(s, dto);
        solicitacaoDao.atualizar(s);
        return detalhar(solicitacaoDao.buscarPorId(id));
    }

    /** Decisão do guardião: só quem é vinculado ao idoso pode responder, e uma única vez. */
    public DetalhesSolicitacaoDto responder(long id, RespostaSolicitacaoDto dto) {
        if (dto == null) {
            throw new RegraNegocioException("Envie a decisão do guardião.");
        }
        Validador.idObrigatorio(dto.getIdGuardiao(), "idGuardiao", "o guardião");
        DecisaoGuardiao decisao = Validador.opcao(DecisaoGuardiao.class, dto.getDecisao(), "decisao", "a decisão");

        SolicitacaoVerificacao s = solicitacaoDao.buscarPorId(id);
        if (!s.estaPendente()) {
            throw new RegraNegocioException(RegraNegocioException.CONFLITO, "Esta solicitação já foi respondida.", null);
        }
        Guardiao guardiao = guardiaoDao.buscarPorId(dto.getIdGuardiao());
        if (!vinculoDao.existe(s.getIdoso().getId(), guardiao.getId())) {
            throw new RegraNegocioException(RegraNegocioException.NAO_PERMITIDO,
                    "Você não é guardião deste idoso.", "idGuardiao");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (decisao == DecisaoGuardiao.BLOQUEAR) {
            String orientacao = Validador.textoObrigatorio(dto.getOrientacao(), "orientacao",
                    "uma orientação para o idoso", 3, 400);
            s.bloquear(guardiao, orientacao, agora);
        } else {
            String orientacao = dto.getOrientacao() == null || dto.getOrientacao().isBlank()
                    ? "Pode seguir."
                    : Validador.textoObrigatorio(dto.getOrientacao(), "orientacao", "a orientação", 1, 400);
            s.liberar(guardiao, orientacao, agora);
        }
        solicitacaoDao.salvarResposta(s);
        return detalhar(solicitacaoDao.buscarPorId(id));
    }

    public void remover(long id) {
        solicitacaoDao.excluir(id);
    }

    /** Quantas vezes um telefone/link/chave já foi bloqueado (aviso mostrado antes de enviar o pedido). */
    public BloqueiosContatoDto bloqueiosDoContato(String contato) {
        String normalizado = Contatos.normalizar(contato);
        if (normalizado == null) {
            throw new RegraNegocioException("Informe o telefone, link ou chave Pix.", "contato");
        }
        return new BloqueiosContatoDto(normalizado, solicitacaoDao.contarBloqueios(normalizado));
    }

    private void preencherDados(SolicitacaoVerificacao s, CadastroSolicitacaoDto dto) {
        TipoSolicitacao tipo = Validador.opcao(TipoSolicitacao.class, dto.getTipo(), "tipo", "o tipo do pedido");
        s.setTipo(tipo);
        s.setDescricao(Validador.textoObrigatorio(dto.getDescricao(), "descricao", "o que aconteceu", 5, 400));

        BigDecimal valor = dto.getValor();
        if (tipo.exigeValor()) {
            s.setValor(Validador.valorPositivo(valor, "valor", "o valor pedido"));
        } else if (valor != null) {
            s.setValor(Validador.valorPositivo(valor, "valor", "o valor"));
        } else {
            s.setValor(null);
        }

        String contato = Contatos.normalizar(dto.getContatoSuspeito());
        if (contato != null && contato.length() > 150) {
            throw new RegraNegocioException("O contato deve ter no máximo 150 caracteres.", "contatoSuspeito");
        }
        s.setContatoSuspeito(contato);
    }

    private DetalhesSolicitacaoDto detalhar(SolicitacaoVerificacao s) {
        Integer bloqueios = s.getContatoSuspeito() == null ? null
                : solicitacaoDao.contarBloqueios(s.getContatoSuspeito());
        return DetalhesSolicitacaoDto.de(s, bloqueios);
    }
}
