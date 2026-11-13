package br.com.fiap.dao;

import br.com.fiap.dto.relatorio.ContatoRecorrenteDto;
import br.com.fiap.dto.relatorio.IdosoSemGuardiaoDto;
import br.com.fiap.dto.relatorio.IndicadoresDto;
import br.com.fiap.dto.relatorio.SolicitacoesPorTipoDto;
import br.com.fiap.dto.relatorio.TempoRespostaDto;
import br.com.fiap.dto.relatorio.TentativasBairroDto;
import br.com.fiap.dto.relatorio.ValorProtegidoDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Relatórios da página de impacto: consultas com JOIN e agregação.
 * São as mesmas consultas do final do script database/guardiao.sql.
 */
public class RelatorioDAO extends BaseDAO {

    public List<ValorProtegidoDto> valorProtegidoPorIdoso() {
        List<ValorProtegidoDto> lista = new ArrayList<>();
        String sql = "SELECT I.NOME, COUNT(S.ID) AS QTD_BLOQUEIOS, NVL(SUM(S.VALOR), 0) AS VALOR_PROTEGIDO "
                + "FROM T_IDOSO I INNER JOIN T_SOLICITACAO S ON S.ID_IDOSO = I.ID "
                + "WHERE S.STATUS = 'BLOQUEADA' "
                + "GROUP BY I.NOME ORDER BY VALOR_PROTEGIDO DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new ValorProtegidoDto(rs.getString("NOME"), rs.getInt("QTD_BLOQUEIOS"),
                        rs.getBigDecimal("VALOR_PROTEGIDO")));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public List<TentativasBairroDto> tentativasNaPortaPorBairro() {
        List<TentativasBairroDto> lista = new ArrayList<>();
        String sql = "SELECT I.BAIRRO, COUNT(*) AS QTD_TENTATIVAS "
                + "FROM T_SOLICITACAO S INNER JOIN T_IDOSO I ON I.ID = S.ID_IDOSO "
                + "WHERE S.TIPO = 'VISITA' "
                + "GROUP BY I.BAIRRO ORDER BY QTD_TENTATIVAS DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new TentativasBairroDto(rs.getString("BAIRRO"), rs.getInt("QTD_TENTATIVAS")));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public List<SolicitacoesPorTipoDto> solicitacoesPorTipo() {
        List<SolicitacoesPorTipoDto> lista = new ArrayList<>();
        String sql = "SELECT TIPO, COUNT(*) AS TOTAL, "
                + "SUM(CASE WHEN STATUS = 'BLOQUEADA' THEN 1 ELSE 0 END) AS BLOQUEADAS, "
                + "SUM(CASE WHEN STATUS = 'LIBERADA' THEN 1 ELSE 0 END) AS LIBERADAS, "
                + "SUM(CASE WHEN STATUS = 'PENDENTE' THEN 1 ELSE 0 END) AS PENDENTES "
                + "FROM T_SOLICITACAO GROUP BY TIPO ORDER BY TOTAL DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new SolicitacoesPorTipoDto(rs.getString("TIPO"), rs.getInt("TOTAL"),
                        rs.getInt("BLOQUEADAS"), rs.getInt("LIBERADAS"), rs.getInt("PENDENTES")));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public List<TempoRespostaDto> tempoMedioDeResposta() {
        List<TempoRespostaDto> lista = new ArrayList<>();
        String sql = "SELECT G.NOME, COUNT(*) AS QTD_RESPOSTAS, "
                + "ROUND(AVG((S.DATA_RESPOSTA - S.DATA_ABERTURA) * 24 * 60), 1) AS MINUTOS_MEDIOS "
                + "FROM T_GUARDIAO G INNER JOIN T_SOLICITACAO S ON S.ID_GUARDIAO_RESPOSTA = G.ID "
                + "WHERE S.DATA_RESPOSTA IS NOT NULL "
                + "GROUP BY G.NOME ORDER BY MINUTOS_MEDIOS";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new TempoRespostaDto(rs.getString("NOME"), rs.getInt("QTD_RESPOSTAS"),
                        rs.getDouble("MINUTOS_MEDIOS")));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public List<ContatoRecorrenteDto> contatosRecorrentes() {
        List<ContatoRecorrenteDto> lista = new ArrayList<>();
        String sql = "SELECT CONTATO_SUSPEITO, MAX(TIPO) AS TIPO, COUNT(DISTINCT ID_IDOSO) AS QTD_IDOSOS, "
                + "COUNT(*) AS QTD_BLOQUEIOS, MAX(DATA_ABERTURA) AS ULTIMO_USO "
                + "FROM T_SOLICITACAO "
                + "WHERE STATUS = 'BLOQUEADA' AND CONTATO_SUSPEITO IN ("
                + "    SELECT CONTATO_SUSPEITO FROM T_SOLICITACAO WHERE CONTATO_SUSPEITO IS NOT NULL "
                + "    GROUP BY CONTATO_SUSPEITO HAVING COUNT(*) > 1) "
                + "GROUP BY CONTATO_SUSPEITO ORDER BY QTD_BLOQUEIOS DESC";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new ContatoRecorrenteDto(rs.getString("CONTATO_SUSPEITO"), rs.getString("TIPO"),
                        rs.getInt("QTD_IDOSOS"), rs.getInt("QTD_BLOQUEIOS"), paraLocalDate(rs.getTimestamp("ULTIMO_USO"))));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    public List<IdosoSemGuardiaoDto> idososSemGuardiao() {
        List<IdosoSemGuardiaoDto> lista = new ArrayList<>();
        String sql = "SELECT I.ID, I.NOME, I.BAIRRO "
                + "FROM T_IDOSO I LEFT JOIN T_VINCULO V ON V.ID_IDOSO = I.ID "
                + "WHERE V.ID_IDOSO IS NULL ORDER BY I.NOME";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new IdosoSemGuardiaoDto(rs.getLong("ID"), rs.getString("NOME"), rs.getString("BAIRRO")));
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return lista;
    }

    /** Números de destaque da página de impacto (cada um é uma subconsulta). */
    public IndicadoresDto indicadores() {
        String sql = "SELECT "
                + "(SELECT NVL(SUM(VALOR), 0) FROM T_SOLICITACAO WHERE STATUS = 'BLOQUEADA') AS VALOR_PROTEGIDO, "
                + "(SELECT COUNT(*) FROM T_SOLICITACAO WHERE STATUS = 'BLOQUEADA') AS GOLPES_EVITADOS, "
                + "(SELECT COUNT(*) FROM T_SOLICITACAO WHERE TIPO = 'VISITA') AS TENTATIVAS_PORTA, "
                + "(SELECT NVL(ROUND(AVG((DATA_RESPOSTA - DATA_ABERTURA) * 24 * 60), 1), 0) "
                + "   FROM T_SOLICITACAO WHERE DATA_RESPOSTA IS NOT NULL) AS MINUTOS_MEDIOS, "
                + "(SELECT COUNT(DISTINCT ID_IDOSO) FROM T_VINCULO) AS IDOSOS_PROTEGIDOS, "
                + "(SELECT COUNT(*) FROM T_IDOSO WHERE ID NOT IN (SELECT ID_IDOSO FROM T_VINCULO)) AS SEM_GUARDIAO, "
                + "(SELECT COUNT(*) FROM T_SOLICITACAO WHERE STATUS = 'PENDENTE') AS PENDENTES "
                + "FROM DUAL";

        try (Connection conn = abrirConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            rs.next();
            return new IndicadoresDto(rs.getBigDecimal("VALOR_PROTEGIDO"), rs.getInt("GOLPES_EVITADOS"),
                    rs.getInt("TENTATIVAS_PORTA"), rs.getDouble("MINUTOS_MEDIOS"), rs.getInt("IDOSOS_PROTEGIDOS"),
                    rs.getInt("SEM_GUARDIAO"), rs.getInt("PENDENTES"));
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }
}
