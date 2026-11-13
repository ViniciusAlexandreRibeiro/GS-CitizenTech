package br.com.fiap.util;

/** Validação e formatação de CPF e CNPJ (dígitos verificadores). */
public final class Documentos {

    private Documentos() {
    }

    /** Remove tudo que não for dígito. Retorna null se a entrada for null. */
    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11 || todosIguais(d)) {
            return false;
        }
        return digitoCpf(d, 9) == d.charAt(9) - '0' && digitoCpf(d, 10) == d.charAt(10) - '0';
    }

    public static boolean cnpjValido(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d == null || d.length() != 14 || todosIguais(d)) {
            return false;
        }
        int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return digitoCnpj(d, peso1) == d.charAt(12) - '0' && digitoCnpj(d, peso2) == d.charAt(13) - '0';
    }

    /** Mostra só o miolo do CPF: ***.456.789-** (privacidade / LGPD). */
    public static String mascararCpf(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11) {
            return "***";
        }
        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }

    public static String formatarCnpj(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d == null || d.length() != 14) {
            return cnpj;
        }
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/"
                + d.substring(8, 12) + "-" + d.substring(12);
    }

    private static int digitoCpf(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digitoCnpj(String d, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (d.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosIguais(String d) {
        return d.chars().distinct().count() == 1;
    }
}
