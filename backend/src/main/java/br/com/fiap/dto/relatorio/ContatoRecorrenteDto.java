package br.com.fiap.dto.relatorio;

import java.time.LocalDate;

public record ContatoRecorrenteDto(String contato, String tipo, int qtIdosos, int qtBloqueios, LocalDate ultimoUso) {
}
