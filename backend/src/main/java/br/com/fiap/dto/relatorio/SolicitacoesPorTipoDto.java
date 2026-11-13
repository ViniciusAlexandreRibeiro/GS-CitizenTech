package br.com.fiap.dto.relatorio;

public record SolicitacoesPorTipoDto(String tipo, int total, int bloqueadas, int liberadas, int pendentes) {
}
