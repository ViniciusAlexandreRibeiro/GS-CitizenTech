package br.com.fiap.dto;

import java.time.LocalDateTime;

/** Dados para agendar, remarcar ou cancelar uma visita. */
public class CadastroVisitaDto {

    private Long idIdoso;
    private Long idPrestador;
    private String nomeVisitante;
    private LocalDateTime dataAgendada;
    private String agendadoPor;
    private String status;

    public CadastroVisitaDto() {
    }

    public Long getIdIdoso() {
        return idIdoso;
    }

    public void setIdIdoso(Long idIdoso) {
        this.idIdoso = idIdoso;
    }

    public Long getIdPrestador() {
        return idPrestador;
    }

    public void setIdPrestador(Long idPrestador) {
        this.idPrestador = idPrestador;
    }

    public String getNomeVisitante() {
        return nomeVisitante;
    }

    public void setNomeVisitante(String nomeVisitante) {
        this.nomeVisitante = nomeVisitante;
    }

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public String getAgendadoPor() {
        return agendadoPor;
    }

    public void setAgendadoPor(String agendadoPor) {
        this.agendadoPor = agendadoPor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
