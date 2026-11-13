package br.com.fiap.models;

import br.com.fiap.models.enums.AgendadoPor;
import br.com.fiap.models.enums.StatusVisita;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

/** Visita agendada na casa do idoso, conferida na porta por um código de 6 dígitos. */
public class Visita implements Verificavel {

    /** O código só vale perto do horário marcado: 2 horas antes ou depois. */
    public static final Duration JANELA_TOLERANCIA = Duration.ofHours(2);

    private static final SecureRandom SORTEIO = new SecureRandom();

    private Long id;
    private Idoso idoso;
    private Prestador prestador;
    private String nomeVisitante;
    private LocalDateTime dataAgendada;
    private String codigo;
    private AgendadoPor agendadoPor;
    private StatusVisita status = StatusVisita.AGENDADA;

    public Visita() {
    }

    /** Gera um código numérico de 6 dígitos (de 000000 a 999999). */
    public static String gerarCodigo() {
        return String.format("%06d", SORTEIO.nextInt(1_000_000));
    }

    /** true se o momento informado está dentro da janela de ±2h do horário agendado. */
    public boolean estaNaJanela(LocalDateTime momento) {
        if (dataAgendada == null || momento == null) {
            return false;
        }
        Duration diferenca = Duration.between(dataAgendada, momento).abs();
        return diferenca.compareTo(JANELA_TOLERANCIA) <= 0;
    }

    /** Uma visita só pode ser confirmada se estiver agendada e dentro da janela. */
    public boolean podeSerConfirmada(LocalDateTime momento) {
        return status == StatusVisita.AGENDADA && estaNaJanela(momento);
    }

    public void confirmar() {
        if (status != StatusVisita.AGENDADA) {
            throw new IllegalStateException("Somente visitas agendadas podem ser confirmadas.");
        }
        status = StatusVisita.CONFIRMADA;
    }

    public void cancelar() {
        if (status == StatusVisita.CONFIRMADA) {
            throw new IllegalStateException("Uma visita já confirmada não pode ser cancelada.");
        }
        status = StatusVisita.CANCELADA;
    }

    /** Nome que aparece para o idoso: o prestador cadastrado ou o visitante avulso. */
    public String getNomeExibicao() {
        if (prestador != null && prestador.getNome() != null) {
            return prestador.getNome();
        }
        return nomeVisitante;
    }

    @Override
    public boolean estaPendente() {
        return status == StatusVisita.AGENDADA;
    }

    @Override
    public String getDescricaoStatus() {
        return switch (status) {
            case AGENDADA -> "Visita agendada";
            case CONFIRMADA -> "Visita confirmada na porta";
            case CANCELADA -> "Visita cancelada";
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

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public AgendadoPor getAgendadoPor() {
        return agendadoPor;
    }

    public void setAgendadoPor(AgendadoPor agendadoPor) {
        this.agendadoPor = agendadoPor;
    }

    public StatusVisita getStatus() {
        return status;
    }

    public void setStatus(StatusVisita status) {
        this.status = status;
    }
}
