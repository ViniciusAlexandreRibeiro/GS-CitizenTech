package br.com.fiap.util;

import java.util.Locale;

/**
 * Padroniza o "contato suspeito" (telefone, link ou chave Pix) para que o mesmo
 * golpista seja reconhecido mesmo quando digitado de formas diferentes.
 */
public final class Contatos {

    private Contatos() {
    }

    public static String normalizar(String contato) {
        if (contato == null || contato.isBlank()) {
            return null;
        }
        String valor = contato.trim();
        String digitos = Documentos.somenteDigitos(valor);
        // Telefone: só números, parênteses, traços, espaços e "+" -> guarda só os dígitos
        if (valor.matches("[\\d\\s()+.\\-]+") && digitos.length() >= 8) {
            if (digitos.startsWith("55") && digitos.length() >= 12) {
                digitos = digitos.substring(2);
            }
            return digitos;
        }
        // Link ou e-mail/chave: minúsculas, sem protocolo, sem "www." e sem barra no fim
        valor = valor.toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "");
        while (valor.endsWith("/")) {
            valor = valor.substring(0, valor.length() - 1);
        }
        return valor;
    }
}
