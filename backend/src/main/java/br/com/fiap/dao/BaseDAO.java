package br.com.fiap.dao;

import br.com.fiap.db.ConnectionFactory;
import br.com.fiap.exception.BancoDadosException;
import br.com.fiap.exception.GuardiaoException;
import br.com.fiap.exception.RegraNegocioException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe base dos DAOs: abre a conexão, converte datas e transforma
 * os erros do Oracle (ORA-xxxxx) em mensagens que o usuário entende.
 */
public abstract class BaseDAO {

    protected Connection abrirConexao() {
        return ConnectionFactory.getConnection();
    }

    protected GuardiaoException traduzirErro(SQLException e) {
        String mensagem = e.getMessage() == null ? "" : e.getMessage().toUpperCase();

        switch (e.getErrorCode()) {
            case 1: // UNIQUE
                if (mensagem.contains("EMAIL")) {
                    return new RegraNegocioException(409, "Já existe um cadastro com este e-mail.", "email");
                }
                if (mensagem.contains("CNPJ")) {
                    return new RegraNegocioException(409, "Já existe uma empresa com este CNPJ.", "cnpj");
                }
                if (mensagem.contains("CODIGO")) {
                    return new RegraNegocioException(409, "Este código de visita já existe.", "codigo");
                }
                return new RegraNegocioException(409, "Já existe um cadastro com este CPF.", "cpf");
            case 2292: // registro filho encontrado (FK)
                return new RegraNegocioException(409, "Não é possível excluir: existem registros ligados a este cadastro.", null);
            case 2291: // registro pai não encontrado (FK)
                return new RegraNegocioException(400, "O registro relacionado informado não existe.", null);
            case 2290: // CHECK
                return new RegraNegocioException(400, "Algum valor está fora do permitido.", null);
            case 1400: // NOT NULL
                return new RegraNegocioException(400, "Preencha todos os campos obrigatórios.", null);
            case 12899: // texto maior que a coluna
                return new RegraNegocioException(400, "Algum texto está maior do que o permitido.", null);
            case 942: // tabela não existe
                return new BancoDadosException("As tabelas do Guardião não existem neste usuário. Rode o script database/guardiao.sql.", e);
            default:
                return new BancoDadosException("Erro ao acessar o banco de dados. Tente novamente.", e);
        }
    }

    protected static Timestamp paraTimestamp(LocalDateTime data) {
        return data == null ? null : Timestamp.valueOf(data);
    }

    protected static LocalDateTime paraLocalDateTime(Timestamp data) {
        return data == null ? null : data.toLocalDateTime();
    }

    protected static LocalDate paraLocalDate(Timestamp data) {
        return data == null ? null : data.toLocalDateTime().toLocalDate();
    }
}
