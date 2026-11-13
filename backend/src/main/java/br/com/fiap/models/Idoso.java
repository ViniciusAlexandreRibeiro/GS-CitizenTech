package br.com.fiap.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/** Pessoa idosa protegida pela rede de guardiões. */
public class Idoso extends Pessoa {

    public static final BigDecimal LIMITE_PIX_PADRAO = new BigDecimal("500.00");

    private String bairro;
    private LocalDate dataNascimento;
    private BigDecimal limitePix = LIMITE_PIX_PADRAO;

    public Idoso() {
    }

    public Idoso(Long id, String nome, String cpf, String telefone, String bairro,
                 LocalDate dataNascimento, BigDecimal limitePix) {
        super(id, nome, cpf, telefone);
        this.bairro = bairro;
        this.dataNascimento = dataNascimento;
        this.limitePix = limitePix;
    }

    @Override
    public String getPapel() {
        return "Idoso";
    }

    public int getIdade() {
        return dataNascimento == null ? 0 : Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    /** true se o valor pedido passa do limite definido pela família. */
    public boolean excedeLimite(BigDecimal valor) {
        return valor != null && limitePix != null && valor.compareTo(limitePix) > 0;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public BigDecimal getLimitePix() {
        return limitePix;
    }

    public void setLimitePix(BigDecimal limitePix) {
        this.limitePix = limitePix;
    }
}
