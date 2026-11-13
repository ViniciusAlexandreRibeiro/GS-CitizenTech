package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Empresa;
import br.com.fiap.models.Prestador;
import br.com.fiap.models.enums.SetorEmpresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração da entidade {@link Prestador} com a tabela T_PRESTADOR.
 * As consultas já trazem a empresa do prestador (JOIN com T_EMPRESA).
 */
public class PrestadorDAO extends BaseDAO {

    private static final String SELECT = "SELECT P.ID, P.NOME, P.CPF, P.FUNCAO, "
            + "E.ID AS ID_EMPRESA, E.NOME AS NOME_EMPRESA, E.CNPJ, E.SETOR "
            + "FROM T_PRESTADOR P INNER JOIN T_EMPRESA E ON E.ID = P.ID_EMPRESA ";

    public void inserir(Prestador prestador) {
        String sql = "INSERT INTO T_PRESTADOR (ID_EMPRESA, NOME, CPF, FUNCAO) VALUES (?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            preencher(stmt, prestador);
            stmt.executeUpdate();

            ResultSet chave = stmt.getGeneratedKeys();
            if (chave.next()) {
                prestador.setId(chave.getLong(1));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Lista todos os prestadores ou só os de uma empresa (idEmpresa diferente de null). */
    public List<Prestador> listar(Long idEmpresa) {
        List<Prestador> lista = new ArrayList<>();
        String sql = SELECT + (idEmpresa == null ? "" : "WHERE P.ID_EMPRESA = ? ") + "ORDER BY P.NOME";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (idEmpresa != null) {
                stmt.setLong(1, idEmpresa);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestador(rs));
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public Prestador buscarPorId(long id) {
        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE P.ID = ?")) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestador(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        throw new EntidadeNaoEncontradaException("Prestador não encontrado.");
    }

    public void atualizar(Prestador prestador) {
        String sql = "UPDATE T_PRESTADOR SET ID_EMPRESA = ?, NOME = ?, CPF = ?, FUNCAO = ? WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencher(stmt, prestador);
            stmt.setLong(5, prestador.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Prestador não encontrado para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long id) {
        String sql = "DELETE FROM T_PRESTADOR WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Prestador não encontrado para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    private void preencher(PreparedStatement stmt, Prestador prestador) throws SQLException {
        stmt.setLong(1, prestador.getEmpresa().getId());
        stmt.setString(2, prestador.getNome());
        stmt.setString(3, prestador.getCpf());
        stmt.setString(4, prestador.getFuncao());
    }

    private Prestador mapearPrestador(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa(rs.getLong("ID_EMPRESA"), rs.getString("NOME_EMPRESA"),
                rs.getString("CNPJ"), SetorEmpresa.valueOf(rs.getString("SETOR")));
        return new Prestador(rs.getLong("ID"), rs.getString("NOME"), rs.getString("CPF"),
                null, empresa, rs.getString("FUNCAO"));
    }
}
