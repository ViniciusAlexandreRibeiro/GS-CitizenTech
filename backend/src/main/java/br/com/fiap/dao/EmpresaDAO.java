package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Empresa;
import br.com.fiap.models.enums.SetorEmpresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração da entidade {@link Empresa} com a tabela T_EMPRESA.
 */
public class EmpresaDAO extends BaseDAO {

    public void inserir(Empresa empresa) {
        String sql = "INSERT INTO T_EMPRESA (NOME, CNPJ, SETOR) VALUES (?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            stmt.setString(1, empresa.getNome());
            stmt.setString(2, empresa.getCnpj());
            stmt.setString(3, empresa.getSetor().name());
            stmt.executeUpdate();

            ResultSet chave = stmt.getGeneratedKeys();
            if (chave.next()) {
                empresa.setId(chave.getLong(1));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public List<Empresa> listarTodos() {
        List<Empresa> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_EMPRESA ORDER BY NOME";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpresa(rs));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public Empresa buscarPorId(long id) {
        String sql = "SELECT * FROM T_EMPRESA WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        throw new EntidadeNaoEncontradaException("Empresa não encontrada.");
    }

    public void atualizar(Empresa empresa) {
        String sql = "UPDATE T_EMPRESA SET NOME = ?, CNPJ = ?, SETOR = ? WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empresa.getNome());
            stmt.setString(2, empresa.getCnpj());
            stmt.setString(3, empresa.getSetor().name());
            stmt.setLong(4, empresa.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Empresa não encontrada para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long id) {
        String sql = "DELETE FROM T_EMPRESA WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Empresa não encontrada para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    private Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        return new Empresa(rs.getLong("ID"), rs.getString("NOME"), rs.getString("CNPJ"),
                SetorEmpresa.valueOf(rs.getString("SETOR")));
    }
}
