package br.com.fiap.models;

import br.com.fiap.models.enums.StatusSolicitacao;
import br.com.fiap.models.enums.TipoSolicitacao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/** Pedido de ajuda do idoso aos guardiões (ou alerta gerado na porta). */
public class SolicitacaoVerificacao implements Verificavel {

    private Long id;
    private Idoso idoso;
    private Guardiao guardiaoResposta;
    private Visita visita;
    private TipoSolicitacao tipo;
    private String descricao;
    private BigDecimal valor;
    private String contatoSuspeito;
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;
    private String orientacao;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataResposta;

    public SolicitacaoVerificacao() {
    }

    /** O guardião autoriza o idoso a seguir em frente. */
    public void liberar(Guardiao guardiao, String orientacao, LocalDateTime momento) {
        responder(guardiao, orientacao, momento, StatusSolicitacao.LIBERADA);
    }

    /** O guardião identifica golpe e orienta o idoso a não seguir. */
    public void bloquear(Guardiao guardiao, String orientacao, LocalDateTime momento) {
        responder(guardiao, orientacao, momento, StatusSolicitacao.BLOQUEADA);
    }

    private void responder(Guardiao guardiao, String orientacao, LocalDateTime momento, StatusSolicitacao novoStatus) {
        if (!estaPendente()) {
            throw new IllegalStateException("Esta solicitação já foi respondida.");
        }
        this.guardiaoResposta = guardiao;
        this.orientacao = orientacao;
        this.dataResposta = momento;
        this.status = novoStatus;
    }

    /** Pedido de dinheiro acima do limite do idoso exige resposta de um guardião. */
    public boolean exigeAprovacaoObrigatoria() {
        return tipo == TipoSolicitacao.PIX && idoso != null && idoso.excedeLimite(valor);
    }

    /** Valor que deixou de ser perdido (só conta quando o guardião bloqueou). */
    public BigDecimal getValorProtegido() {
        return status == StatusSolicitacao.BLOQUEADA && valor != null ? valor : BigDecimal.ZERO;
    }

    /** Minutos entre a abertura e a resposta, ou null se ainda não respondida. */
    public Long getMinutosAteResposta() {
        if (dataAbertura == null || dataResposta == null) {
            return null;
        }
        return Duration.between(dataAbertura, dataResposta).toMinutes();
    }

    @Override
    public boolean estaPendente() {
        return status == StatusSolicitacao.PENDENTE;
    }

    @Override
    public String getDescricaoStatus() {
        return switch (status) {
            case PENDENTE -> "Aguardando um guardião";
            case LIBERADA -> "Liberado pelo guardião";
            case BLOQUEADA -> "Bloqueado: é golpe";
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Idoso getIdoso() {
        return idoso;
    }

    public void setIdoso(Idoso idoso) {
        this.idoso = idoso;
    }

    public Guardiao getGuardiaoResposta() {
        return guardiaoResposta;
    }

    public void setGuardiaoResposta(Guardiao guardiaoResposta) {
        this.guardiaoResposta = guardiaoResposta;
    }

    public Visita getVisita() {
        return visita;
    }

    public void setVisita(Visita visita) {
        this.visita = visita;
    }

    public TipoSolicitacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoSolicitacao tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getContatoSuspeito() {
        return contatoSuspeito;
    }

    public void setContatoSuspeito(String contatoSuspeito) {
        this.contatoSuspeito = contatoSuspeito;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public String getOrientacao() {
        return orientacao;
    }

    public void setOrientacao(String orientacao) {
        this.orientacao = orientacao;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }
}
