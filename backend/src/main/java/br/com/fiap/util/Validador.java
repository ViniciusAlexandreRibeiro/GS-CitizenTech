package br.com.fiap.util;

import br.com.fiap.exception.RegraNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

/** Validações de entrada reaproveitadas pelos services. Todas lançam RegraNegocioException (400). */
public final class Validador {

    private Validador() {
    }

    public static String textoObrigatorio(String valor, String campo, String rotulo, int minimo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Informe " + rotulo + ".", campo);
        }
        String limpo = valor.trim().replaceAll("\\s+", " ");
        if (limpo.length() < minimo) {
            throw new RegraNegocioException(primeiraMaiuscula(rotulo) + " deve ter pelo menos " + minimo + " caracteres.", campo);
        }
        if (limpo.length() > maximo) {
            throw new RegraNegocioException(primeiraMaiuscula(rotulo) + " deve ter no máximo " + maximo + " caracteres.", campo);
        }
        return limpo;
    }

    public static String cpf(String valor) {
        if (!Documentos.cpfValido(valor)) {
            throw new RegraNegocioException("CPF inválido: confira os números digitados.", "cpf");
        }
        return Documentos.somenteDigitos(valor);
    }

    public static String cnpj(String valor) {
        if (!Documentos.cnpjValido(valor)) {
            throw new RegraNegocioException("CNPJ inválido: confira os números digitados.", "cnpj");
        }
        return Documentos.somenteDigitos(valor);
    }

    /** Telefone com DDD: 10 dígitos (fixo) ou 11 dígitos (celular). */
    public static String telefone(String valor) {
        String digitos = Documentos.somenteDigitos(valor);
        if (digitos == null || digitos.length() < 10 || digitos.length() > 11) {
            throw new RegraNegocioException("Telefone inválido: informe o DDD e o número.", "telefone");
        }
        return digitos;
    }

    public static String email(String valor) {
        String email = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$") || email.length() > 100) {
            throw new RegraNegocioException("E-mail inválido.", "email");
        }
        return email;
    }

    public static LocalDate dataNoPassado(LocalDate data, String campo, String rotulo) {
        if (data == null) {
            throw new RegraNegocioException("Informe " + rotulo + ".", campo);
        }
        if (!data.isBefore(LocalDate.now())) {
            throw new RegraNegocioException(primeiraMaiuscula(rotulo) + " deve estar no passado.", campo);
        }
        return data;
    }

    public static BigDecimal valorPositivo(BigDecimal valor, String campo, String rotulo) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Informe " + rotulo + " maior que zero.", campo);
        }
        return valor;
    }

    public static Long idObrigatorio(Long id, String campo, String rotulo) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("Selecione " + rotulo + ".", campo);
        }
        return id;
    }

    /** Converte o texto em um valor do enum, com mensagem amigável se não existir. */
    public static <E extends Enum<E>> E opcao(Class<E> tipo, String valor, String campo, String rotulo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Informe " + rotulo + ".", campo);
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Opção inválida para " + rotulo + ".", campo);
        }
    }

    private static String primeiraMaiuscula(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
