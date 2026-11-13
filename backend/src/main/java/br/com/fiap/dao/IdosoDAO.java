package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Idoso;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integração da entidade {@link Idoso} com a tabela T_IDOSO.
 */
public class IdosoDAO extends BaseDAO {

    public void inserir(Idoso idoso) {
        String sql = "INSERT INTO T_IDOSO (NOME, CPF, TELEFONE, BAIRRO, DATA_NASCIMENTO, LIMITE_PIX) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            preencher(stmt, idoso);
            stmt.executeUpdate();

            ResultSet chave = stmt.getGeneratedKeys();
            if (chave.next()) {
                idoso.setId(chave.getLong(1));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public List<Idoso> listarTodos() {
        List<Idoso> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_IDOSO ORDER BY NOME";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearIdoso(rs));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public Idoso buscarPorId(long id) {
        String sql = "SELECT * FROM T_IDOSO WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearIdoso(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        throw new EntidadeNaoEncontradaException("Idoso não encontrado.");
    }

    public void atualizar(Idoso idoso) {
        String sql = "UPDATE T_IDOSO SET NOME = ?, CPF = ?, TELEFONE = ?, BAIRRO = ?, DATA_NASCIMENTO = ?, LIMITE_PIX = ? WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencher(stmt, idoso);
            stmt.setLong(7, idoso.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Idoso não encontrado para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long id) {
        String sql = "DELETE FROM T_IDOSO WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Idoso não encontrado para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Quantos guardiões cada idoso tem (chave = id do idoso). */
    public Map<Long, Integer> contarGuardioes() {
        Map<Long, Integer> mapa = new HashMap<>();
        String sql = "SELECT I.ID, COUNT(V.ID_GUARDIAO) AS QTD "
                + "FROM T_IDOSO I LEFT JOIN T_VINCULO V ON V.ID_IDOSO = I.ID "
                + "GROUP BY I.ID";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getLong("ID"), rs.getInt("QTD"));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return mapa;
    }

    private void preencher(PreparedStatement stmt, Idoso idoso) throws SQLException {
        stmt.setString(1, idoso.getNome());
        stmt.setString(2, idoso.getCpf());
        stmt.setString(3, idoso.getTelefone());
        stmt.setString(4, idoso.getBairro());
        stmt.setDate(5, Date.valueOf(idoso.getDataNascimento()));
        stmt.setBigDecimal(6, idoso.getLimitePix());
    }

    private Idoso mapearIdoso(ResultSet rs) throws SQLException {
        return new Idoso(rs.getLong("ID"), rs.getString("NOME"), rs.getString("CPF"), rs.getString("TELEFONE"),
                rs.getString("BAIRRO"), paraLocalDate(rs.getTimestamp("DATA_NASCIMENTO")), rs.getBigDecimal("LIMITE_PIX"));
    }
}
