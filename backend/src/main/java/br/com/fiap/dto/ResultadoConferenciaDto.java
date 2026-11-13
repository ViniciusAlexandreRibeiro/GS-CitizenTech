package br.com.fiap.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Resposta da conferência na porta: CONFIRMADA (pode abrir) ou NEGADA (não abra). */
public record ResultadoConferenciaDto(String resultado, String mensagem, String visitante, String funcao,
                                      String empresa, LocalDateTime horario, Long idSolicitacao,
                                      List<String> guardioesAvisados) {

    public static ResultadoConferenciaDto confirmada(DetalhesVisitaDto v) {
        return new ResultadoConferenciaDto("CONFIRMADA", "Pode abrir. Visita confirmada.", v.visitante(),
                v.funcao(), v.empresa(), v.dataAgendada(), null, List.of());
    }

    public static ResultadoConferenciaDto negada(String motivo, Long idSolicitacao, List<String> guardioes) {
        return new ResultadoConferenciaDto("NEGADA", "Não abra a porta. " + motivo, null, null, null, null,
                idSolicitacao, guardioes);
    }
}
