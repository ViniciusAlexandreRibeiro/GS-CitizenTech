package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Guardiao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração da entidade {@link Guardiao} com a tabela T_GUARDIAO.
 */
public class GuardiaoDAO extends BaseDAO {

    public void inserir(Guardiao guardiao) {
        String sql = "INSERT INTO T_GUARDIAO (NOME, CPF, TELEFONE, EMAIL) VALUES (?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            preencher(stmt, guardiao);
            stmt.executeUpdate();

            ResultSet chave = stmt.getGeneratedKeys();
            if (chave.next()) {
                guardiao.setId(chave.getLong(1));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public List<Guardiao> listarTodos() {
        List<Guardiao> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_GUARDIAO ORDER BY NOME";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearGuardiao(rs));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public Guardiao buscarPorId(long id) {
        String sql = "SELECT * FROM T_GUARDIAO WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearGuardiao(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        throw new EntidadeNaoEncontradaException("Guardião não encontrado.");
    }

    public void atualizar(Guardiao guardiao) {
        String sql = "UPDATE T_GUARDIAO SET NOME = ?, CPF = ?, TELEFONE = ?, EMAIL = ? WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencher(stmt, guardiao);
            stmt.setLong(5, guardiao.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Guardião não encontrado para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long id) {
        String sql = "DELETE FROM T_GUARDIAO WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Guardião não encontrado para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    private void preencher(PreparedStatement stmt, Guardiao guardiao) throws SQLException {
        stmt.setString(1, guardiao.getNome());
        stmt.setString(2, guardiao.getCpf());
        stmt.setString(3, guardiao.getTelefone());
        stmt.setString(4, guardiao.getEmail());
    }

    private Guardiao mapearGuardiao(ResultSet rs) throws SQLException {
        return new Guardiao(rs.getLong("ID"), rs.getString("NOME"), rs.getString("CPF"),
                rs.getString("TELEFONE"), rs.getString("EMAIL"));
    }
}
