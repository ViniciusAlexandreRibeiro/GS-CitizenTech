package br.com.fiap.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados enviados para cadastrar ou atualizar um idoso. */
public class CadastroIdosoDto {

    private String nome;
    private String cpf;
    private String telefone;
    private String bairro;
    private LocalDate dataNascimento;
    private BigDecimal limitePix;

    public CadastroIdosoDto() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
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
