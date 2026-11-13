package br.com.fiap.dao;

import br.com.fiap.exception.EntidadeNaoEncontradaException;
import br.com.fiap.models.Guardiao;
import br.com.fiap.models.Idoso;
import br.com.fiap.models.SolicitacaoVerificacao;
import br.com.fiap.models.Visita;
import br.com.fiap.models.enums.StatusSolicitacao;
import br.com.fiap.models.enums.TipoSolicitacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração da entidade {@link SolicitacaoVerificacao} com a tabela T_SOLICITACAO.
 */
public class SolicitacaoDAO extends BaseDAO {

    private static final String SELECT = "SELECT S.*, I.NOME AS NOME_IDOSO, I.BAIRRO, I.LIMITE_PIX, G.NOME AS NOME_GUARDIAO "
            + "FROM T_SOLICITACAO S "
            + "INNER JOIN T_IDOSO I ON I.ID = S.ID_IDOSO "
            + "LEFT JOIN T_GUARDIAO G ON G.ID = S.ID_GUARDIAO_RESPOSTA ";

    public void inserir(SolicitacaoVerificacao solicitacao) {
        String sql = "INSERT INTO T_SOLICITACAO (ID_IDOSO, ID_VISITA, TIPO, DESCRICAO, VALOR, CONTATO_SUSPEITO, STATUS, DATA_ABERTURA) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {

            stmt.setLong(1, solicitacao.getIdoso().getId());
            if (solicitacao.getVisita() == null) {
                stmt.setNull(2, Types.NUMERIC);
            } else {
                stmt.setLong(2, solicitacao.getVisita().getId());
            }
            stmt.setString(3, solicitacao.getTipo().name());
            stmt.setString(4, solicitacao.getDescricao());
            stmt.setBigDecimal(5, solicitacao.getValor());
            stmt.setString(6, solicitacao.getContatoSuspeito());
            stmt.setString(7, solicitacao.getStatus().name());
            stmt.setTimestamp(8, paraTimestamp(solicitacao.getDataAbertura()));
            stmt.executeUpdate();

            ResultSet chave = stmt.getGeneratedKeys();
            if (chave.next()) {
                solicitacao.setId(chave.getLong(1));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /**
     * Lista com filtros opcionais. O filtro por guardião traz os pedidos de todos
     * os idosos que ele protege. Os pendentes aparecem primeiro.
     */
    public List<SolicitacaoVerificacao> listar(Long idIdoso, Long idGuardiao, StatusSolicitacao status) {
        List<SolicitacaoVerificacao> lista = new ArrayList<>();
        String sql = SELECT + "WHERE 1 = 1 ";
        if (idIdoso != null) {
            sql += "AND S.ID_IDOSO = ? ";
        }
        if (idGuardiao != null) {
            sql += "AND S.ID_IDOSO IN (SELECT ID_IDOSO FROM T_VINCULO WHERE ID_GUARDIAO = ?) ";
        }
        if (status != null) {
            sql += "AND S.STATUS = ? ";
        }
        sql += "ORDER BY CASE WHEN S.STATUS = 'PENDENTE' THEN 0 ELSE 1 END, S.DATA_ABERTURA DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int posicao = 1;
            if (idIdoso != null) {
                stmt.setLong(posicao++, idIdoso);
            }
            if (idGuardiao != null) {
                stmt.setLong(posicao++, idGuardiao);
            }
            if (status != null) {
                stmt.setString(posicao, status.name());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitacao(rs));
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public SolicitacaoVerificacao buscarPorId(long id) {
        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE S.ID = ?")) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearSolicitacao(rs);
                }
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        throw new EntidadeNaoEncontradaException("Solicitação não encontrada.");
    }

    /** Atualiza o que o idoso escreveu (só enquanto ninguém respondeu). */
    public void atualizar(SolicitacaoVerificacao solicitacao) {
        String sql = "UPDATE T_SOLICITACAO SET TIPO = ?, DESCRICAO = ?, VALOR = ?, CONTATO_SUSPEITO = ? WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, solicitacao.getTipo().name());
            stmt.setString(2, solicitacao.getDescricao());
            stmt.setBigDecimal(3, solicitacao.getValor());
            stmt.setString(4, solicitacao.getContatoSuspeito());
            stmt.setLong(5, solicitacao.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Solicitação não encontrada para atualizar.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Grava a decisão do guardião (liberar ou bloquear). */
    public void salvarResposta(SolicitacaoVerificacao solicitacao) {
        String sql = "UPDATE T_SOLICITACAO SET STATUS = ?, ID_GUARDIAO_RESPOSTA = ?, ORIENTACAO = ?, DATA_RESPOSTA = ? "
                + "WHERE ID = ? AND STATUS = 'PENDENTE'";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, solicitacao.getStatus().name());
            stmt.setLong(2, solicitacao.getGuardiaoResposta().getId());
            stmt.setString(3, solicitacao.getOrientacao());
            stmt.setTimestamp(4, paraTimestamp(solicitacao.getDataResposta()));
            stmt.setLong(5, solicitacao.getId());

            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Solicitação pendente não encontrada.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluir(long id) {
        String sql = "DELETE FROM T_SOLICITACAO WHERE ID = ?";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new EntidadeNaoEncontradaException("Solicitação não encontrada para excluir.");
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    /** Quantas vezes este contato já foi bloqueado por algum guardião. */
    public int contarBloqueios(String contato) {
        String sql = "SELECT COUNT(*) FROM T_SOLICITACAO WHERE CONTATO_SUSPEITO = ? AND STATUS = 'BLOQUEADA'";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contato);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    private SolicitacaoVerificacao mapearSolicitacao(ResultSet rs) throws SQLException {
        SolicitacaoVerificacao s = new SolicitacaoVerificacao();
        s.setId(rs.getLong("ID"));
        s.setTipo(TipoSolicitacao.valueOf(rs.getString("TIPO")));
        s.setDescricao(rs.getString("DESCRICAO"));
        s.setValor(rs.getBigDecimal("VALOR"));
        s.setContatoSuspeito(rs.getString("CONTATO_SUSPEITO"));
        s.setStatus(StatusSolicitacao.valueOf(rs.getString("STATUS")));
        s.setOrientacao(rs.getString("ORIENTACAO"));
        s.setDataAbertura(paraLocalDateTime(rs.getTimestamp("DATA_ABERTURA")));
        s.setDataResposta(paraLocalDateTime(rs.getTimestamp("DATA_RESPOSTA")));

        Idoso idoso = new Idoso();
        idoso.setId(rs.getLong("ID_IDOSO"));
        idoso.setNome(rs.getString("NOME_IDOSO"));
        idoso.setBairro(rs.getString("BAIRRO"));
        idoso.setLimitePix(rs.getBigDecimal("LIMITE_PIX"));
        s.setIdoso(idoso);

        long idGuardiao = rs.getLong("ID_GUARDIAO_RESPOSTA");
        if (!rs.wasNull()) {
            Guardiao guardiao = new Guardiao();
            guardiao.setId(idGuardiao);
            guardiao.setNome(rs.getString("NOME_GUARDIAO"));
            s.setGuardiaoResposta(guardiao);
        }

        long idVisita = rs.getLong("ID_VISITA");
        if (!rs.wasNull()) {
            Visita visita = new Visita();
            visita.setId(idVisita);
            s.setVisita(visita);
        }
        return s;
    }
}
