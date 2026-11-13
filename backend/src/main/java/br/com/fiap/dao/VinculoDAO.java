package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Guardiao;
import br.com.fiap.models.Idoso;
import br.com.fiap.models.VinculoGuardiao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração com a tabela associativa T_VINCULO (N:N entre idoso e guardião).
 * A chave primária é composta: ID_IDOSO + ID_GUARDIAO.
 */
public class VinculoDAO extends BaseDAO {

    private static final String SELECT = "SELECT V.ID_IDOSO, I.NOME AS NOME_IDOSO, V.ID_GUARDIAO, "
            + "G.NOME AS NOME_GUARDIAO, G.TELEFONE, G.EMAIL, V.PARENTESCO, V.PRIORIDADE, V.DATA_VINCULO "
            + "FROM T_VINCULO V "
            + "INNER JOIN T_IDOSO I ON I.ID = V.ID_IDOSO "
            + "INNER JOIN T_GUARDIAO G ON G.ID = V.ID_GUARDIAO ";

    public void inserir(VinculoGuardiao vinculo) {
        String sql = "INSERT INTO T_VINCULO (ID_IDOSO, ID_GUARDIAO, PARENTESCO, PRIORIDADE) VALUES (?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vinculo.getIdoso().getId());
            stmt.setLong(2, vinculo.getGuardiao().getId());
            stmt.setString(3, vinculo.getParentesco());
            stmt.setInt(4, vinculo.getPrioridade());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Filtra por idoso e/ou guardião; quando os dois são null, lista todos. */
    public List<VinculoGuardiao> listar(Long idIdoso, Long idGuardiao) {
        List<VinculoGuardiao> lista = new ArrayList<>();
        String sql = SELECT + "WHERE 1 = 1 ";
        if (idIdoso != null) {
            sql += "AND V.ID_IDOSO = ? ";
        }
        if (idGuardiao != null) {
            sql += "AND V.ID_GUARDIAO = ? ";
        }
        sql += "ORDER BY I.NOME, V.PRIORIDADE";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int posicao = 1;
            if (idIdoso != null) {
                stmt.setLong(posicao++, idIdoso);
            }
            if (idGuardiao != null) {
                stmt.setLong(posicao, idGuardiao);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVinculo(rs));
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public VinculoGuardiao buscar(long idIdoso, long idGuardiao) {
        List<VinculoGuardiao> encontrados = listar(idIdoso, idGuardiao);
        if (encontrados.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Vínculo entre este idoso e este guardião não encontrado.");
        }
        return encontrados.get(0);
    }

    public boolean existe(long idIdoso, long idGuardiao) {
        return !listar(idIdoso, idGuardiao).isEmpty();
    }

    public int contarGuardioesDoIdoso(long idIdoso) {
        return listar(idIdoso, null).size();
    }

    public void atualizar(VinculoGuardiao vinculo) {
        String sql = "UPDATE T_VINCULO SET PARENTESCO = ?, PRIORIDADE = ? WHERE ID_IDOSO = ? AND ID_GUARDIAO = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vinculo.getParentesco());
            stmt.setInt(2, vinculo.getPrioridade());
            stmt.setLong(3, vinculo.getIdoso().getId());
            stmt.setLong(4, vinculo.getGuardiao().getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Vínculo não encontrado para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long idIdoso, long idGuardiao) {
        String sql = "DELETE FROM T_VINCULO WHERE ID_IDOSO = ? AND ID_GUARDIAO = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idIdoso);
            stmt.setLong(2, idGuardiao);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Vínculo não encontrado para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    private VinculoGuardiao mapearVinculo(ResultSet rs) throws SQLException {
        Idoso idoso = new Idoso();
        idoso.setId(rs.getLong("ID_IDOSO"));
        idoso.setNome(rs.getString("NOME_IDOSO"));

        Guardiao guardiao = new Guardiao(rs.getLong("ID_GUARDIAO"), rs.getString("NOME_GUARDIAO"), null,
                rs.getString("TELEFONE"), rs.getString("EMAIL"));

        return new VinculoGuardiao(idoso, guardiao, rs.getString("PARENTESCO"), rs.getInt("PRIORIDADE"),
                paraLocalDate(rs.getTimestamp("DATA_VINCULO")));
    }
}
