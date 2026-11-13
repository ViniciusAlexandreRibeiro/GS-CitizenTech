package br.com.fiap.db;

import br.com.fiap.exception.BancoDadosException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexões com o Oracle.
 *
 * As credenciais NÃO ficam no código: são lidas do arquivo db.properties
 * (copie o db.properties.example e preencha com o seu RM e senha) ou das
 * variáveis de ambiente GDN_DB_URL, GDN_DB_USUARIO e GDN_DB_SENHA.
 */
public final class ConnectionFactory {

    private static final String URL_PADRAO = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";
    private static Properties config;

    private ConnectionFactory() {
    }

    public static Connection getConnection() {
        Properties p = carregarConfiguracao();
        try {
            DriverManager.setLoginTimeout(10);
            return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.usuario"), p.getProperty("db.senha"));
        } catch (SQLException e) {
            throw new BancoDadosException(mensagemDeConexao(e), e);
        }
    }

    private static synchronized Properties carregarConfiguracao() {
        if (config != null) {
            return config;
        }
        Properties p = new Properties();
        Path arquivo = localizarArquivo();
        if (arquivo != null) {
            try (InputStream in = Files.newInputStream(arquivo)) {
                p.load(in);
            } catch (IOException e) {
                throw new BancoDadosException("Não foi possível ler o arquivo " + arquivo + ".", e);
            }
        }
        sobrescreverComAmbiente(p, "db.url", "GDN_DB_URL");
        sobrescreverComAmbiente(p, "db.usuario", "GDN_DB_USUARIO");
        sobrescreverComAmbiente(p, "db.senha", "GDN_DB_SENHA");
        p.putIfAbsent("db.url", URL_PADRAO);

        if (vazio(p.getProperty("db.usuario")) || vazio(p.getProperty("db.senha"))) {
            throw new BancoDadosException("Credenciais do banco não configuradas. Copie backend/db.properties.example "
                    + "para backend/db.properties e preencha db.usuario e db.senha.", null);
        }
        config = p;
        return config;
    }

    /** Procura o db.properties na pasta atual ou na pasta backend/ (quando rodado da raiz do projeto). */
    private static Path localizarArquivo() {
        String indicado = System.getProperty("db.config");
        if (indicado != null && Files.exists(Path.of(indicado))) {
            return Path.of(indicado);
        }
        for (Path candidato : new Path[]{Path.of("db.properties"), Path.of("backend", "db.properties")}) {
            if (Files.exists(candidato)) {
                return candidato;
            }
        }
        return null;
    }

    private static void sobrescreverComAmbiente(Properties p, String chave, String variavel) {
        String valor = System.getenv(variavel);
        if (!vazio(valor)) {
            p.setProperty(chave, valor);
        }
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String mensagemDeConexao(SQLException e) {
        return switch (e.getErrorCode()) {
            case 1017 -> "Usuário ou senha do banco incorretos. Confira o db.properties.";
            case 28000 -> "A conta do banco está bloqueada.";
            default -> "Não foi possível acessar o banco de dados. Verifique a conexão e tente novamente.";
        };
    }
}
