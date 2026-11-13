package br.com.fiap.models.enums;

/** Tipos de situação suspeita que o idoso pode levar aos guardiões. */
public enum TipoSolicitacao {
    VISITA("Alguém na porta"),
    PIX("Pedido de dinheiro"),
    LIGACAO("Ligação"),
    LINK("Link"),
    MENSAGEM("Mensagem");

    private final String descricao;

    TipoSolicitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Tipos em que o valor em dinheiro é obrigatório. */
    public boolean exigeValor() {
        return this == PIX;
    }
}
