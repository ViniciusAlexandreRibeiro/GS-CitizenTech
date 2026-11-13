package br.com.fiap.dto;

/** Formato único de erro devolvido pela API (a web mostra a mensagem na página). */
public record ErroDto(int status, String erro, String mensagem, String campo) {
}
