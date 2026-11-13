package br.com.fiap.dto;

/** Código que o idoso digita quando alguém está na porta. */
public class ConferenciaVisitaDto {

    private Long idIdoso;
    private String codigo;

    public ConferenciaVisitaDto() {
    }

    public Long getIdIdoso() {
        return idIdoso;
    }

    public void setIdIdoso(Long idIdoso) {
        this.idIdoso = idIdoso;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
