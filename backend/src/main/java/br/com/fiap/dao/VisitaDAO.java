package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Empresa;
import br.com.fiap.models.Idoso;
import br.com.fiap.models.Prestador;
import br.com.fiap.models.Visita;
import br.com.fiap.models.enums.AgendadoPor;
import br.com.fiap.models.enums.StatusVisita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração da entidade {@link Visita} com a tabela T_VISITA.
 * As consultas trazem o idoso e, quando houver, o prestador e a empresa.
 */
public class VisitaDAO extends BaseDAO {

    private static final String SELECT = "SELECT V.ID, V.NOME_VISITANTE, V.DATA_AGENDADA, V.CODIGO, V.AGENDADO_POR, V.STATUS, "
            + "I.ID AS ID_IDOSO, I.NOME AS NOME_IDOSO, I.BAIRRO, "
            + "P.ID AS ID_PRESTADOR, P.NOME AS NOME_PRESTADOR, P.FUNCAO, "
            + "E.ID AS ID_EMPRESA, E.NOME AS NOME_EMPRESA "
            + "FROM T_VISITA V "
            + "INNER JOIN T_IDOSO I ON I.ID = V.ID_IDOSO "
            + "LEFT JOIN T_PRESTADOR P ON P.ID = V.ID_PRESTADOR "
            + "LEFT JOIN T_EMPRESA E ON E.ID = P.ID_EMPRESA ";

    public void inserir(Visita visita) {
        String sql = "INSERT INTO T_VISITA (ID_IDOSO, ID_PRESTADOR, NOME_VISITANTE, DATA_AGENDADA, CODIGO, AGENDADO_POR, STATUS) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            preencher(stmt, visita);
            stmt.setString(5, visita.getCodigo());
            stmt.setString(6, visita.getAgendadoPor().name());
            stmt.setString(7, visita.getStatus().name());
            stmt.executeUpdate();

            ResultSet chave = stmt.getGeneratedKeys();
            if (chave.next()) {
                visita.setId(chave.getLong(1));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Lista as visitas, com filtro opcional por idoso e por status. */
    public List<Visita> listar(Long idIdoso, StatusVisita status) {
        List<Visita> lista = new ArrayList<>();
        String sql = SELECT + "WHERE 1 = 1 ";
        if (idIdoso != null) {
            sql += "AND V.ID_IDOSO = ? ";
        }
        if (status != null) {
            sql += "AND V.STATUS = ? ";
        }
        sql += "ORDER BY V.DATA_AGENDADA DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int posicao = 1;
            if (idIdoso != null) {
                stmt.setLong(posicao++, idIdoso);
            }
            if (status != null) {
                stmt.setString(posicao, status.name());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVisita(rs));
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public Visita buscarPorId(long id) {
        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE V.ID = ?")) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVisita(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        throw new EntidadeNaoEncontradaException("Visita não encontrada.");
    }

    /** Busca pelo código de 6 dígitos. Retorna null quando o código não existe. */
    public Visita buscarPorCodigo(String codigo) {
        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE V.CODIGO = ?")) {

            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVisita(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return null;
    }

    public void atualizar(Visita visita) {
        String sql = "UPDATE T_VISITA SET ID_IDOSO = ?, ID_PRESTADOR = ?, NOME_VISITANTE = ?, DATA_AGENDADA = ?, "
                + "AGENDADO_POR = ?, STATUS = ? WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencher(stmt, visita);
            stmt.setString(5, visita.getAgendadoPor().name());
            stmt.setString(6, visita.getStatus().name());
            stmt.setLong(7, visita.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Visita não encontrada para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long id) {
        String sql = "DELETE FROM T_VISITA WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Visita não encontrada para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Preenche os 4 primeiros parâmetros, iguais no INSERT e no UPDATE. */
    private void preencher(PreparedStatement stmt, Visita visita) throws SQLException {
        stmt.setLong(1, visita.getIdoso().getId());
        if (visita.getPrestador() == null) {
            stmt.setNull(2, Types.NUMERIC);
        } else {
            stmt.setLong(2, visita.getPrestador().getId());
        }
        stmt.setString(3, visita.getNomeVisitante());
        stmt.setTimestamp(4, paraTimestamp(visita.getDataAgendada()));
    }

    private Visita mapearVisita(ResultSet rs) throws SQLException {
        Visita visita = new Visita();
        visita.setId(rs.getLong("ID"));
        visita.setNomeVisitante(rs.getString("NOME_VISITANTE"));
        visita.setDataAgendada(paraLocalDateTime(rs.getTimestamp("DATA_AGENDADA")));
        visita.setCodigo(rs.getString("CODIGO"));
        visita.setAgendadoPor(AgendadoPor.valueOf(rs.getString("AGENDADO_POR")));
        visita.setStatus(StatusVisita.valueOf(rs.getString("STATUS")));

        Idoso idoso = new Idoso();
        idoso.setId(rs.getLong("ID_IDOSO"));
        idoso.setNome(rs.getString("NOME_IDOSO"));
        idoso.setBairro(rs.getString("BAIRRO"));
        visita.setIdoso(idoso);

        long idPrestador = rs.getLong("ID_PRESTADOR");
        if (!rs.wasNull()) {
            Empresa empresa = new Empresa();
            empresa.setId(rs.getLong("ID_EMPRESA"));
            empresa.setNome(rs.getString("NOME_EMPRESA"));

            Prestador prestador = new Prestador();
            prestador.setId(idPrestador);
            prestador.setNome(rs.getString("NOME_PRESTADOR"));
            prestador.setFuncao(rs.getString("FUNCAO"));
            prestador.setEmpresa(empresa);
            visita.setPrestador(prestador);
        }
        return visita;
    }
}
