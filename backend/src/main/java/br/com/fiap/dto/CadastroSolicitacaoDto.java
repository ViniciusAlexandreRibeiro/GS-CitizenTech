package br.com.fiap.dto;

import java.math.BigDecimal;

/** Pedido de ajuda aberto pelo idoso. */
public class CadastroSolicitacaoDto {

    private Long idIdoso;
    private String tipo;
    private String descricao;
    private BigDecimal valor;
    private String contatoSuspeito;

    public CadastroSolicitacaoDto() {
    }

    public Long getIdIdoso() {
        return idIdoso;
    }

    public void setIdIdoso(Long idIdoso) {
        this.idIdoso = idIdoso;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
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
}
