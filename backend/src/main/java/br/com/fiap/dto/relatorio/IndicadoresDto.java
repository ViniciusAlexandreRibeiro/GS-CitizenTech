package br.com.fiap.dto.relatorio;

import java.math.BigDecimal;

/** Números de destaque da página de impacto. */
public record IndicadoresDto(BigDecimal valorProtegido, int golpesEvitados, int tentativasNaPorta,
                             double minutosMedioResposta, int idososProtegidos, int idososSemGuardiao,
                             int pendentes) {
}
