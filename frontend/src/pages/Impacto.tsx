import { useEffect, useState, type ReactNode } from "react";
import { Carregando, Mensagem, Pagina } from "../components/Ui";
import { api, mensagemDeErro } from "../services/api";
import type {
  ContatoRecorrente, IdosoSemGuardiao, Indicadores, SolicitacoesPorTipo, TempoResposta, TentativasBairro, ValorProtegido,
} from "../types";
import { formatarContato, formatarData, formatarMoeda, NOMES_TIPO } from "../utils/formatos";

interface Dados {
  indicadores: Indicadores;
  valor: ValorProtegido[];
  bairros: TentativasBairro[];
  tipos: SolicitacoesPorTipo[];
  tempos: TempoResposta[];
  contatos: ContatoRecorrente[];
  semGuardiao: IdosoSemGuardiao[];
}

function Cartao({ titulo, valor, detalhe, cor }: { titulo: string; valor: string; detalhe: string; cor: string }) {
  return (
    <div className="rounded-2xl border border-stone-200 bg-white p-4">
      <p className="text-sm font-semibold text-stone-600">{titulo}</p>
      <p className={`mt-1 text-3xl font-black ${cor}`}>{valor}</p>
      <p className="text-sm text-stone-500">{detalhe}</p>
    </div>
  );
}

function Bloco({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className="rounded-2xl border border-stone-200 bg-white p-5">
      <h2 className="mb-4 text-lg font-extrabold">{titulo}</h2>
      {children}
    </section>
  );
}

export function Impacto() {
  const [dados, setDados] = useState<Dados | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    Promise.all([
      api.relatorios.indicadores(), api.relatorios.valorProtegido(), api.relatorios.portaPorBairro(),
      api.relatorios.porTipo(), api.relatorios.tempoResposta(), api.relatorios.contatosRecorrentes(),
      api.relatorios.idososSemGuardiao(),
    ])
      .then(([indicadores, valor, bairros, tipos, tempos, contatos, semGuardiao]) =>
        setDados({ indicadores, valor, bairros, tipos, tempos, contatos, semGuardiao }))
      .catch((e) => setErro(mensagemDeErro(e)));
  }, []);

  if (erro) {
    return <Pagina titulo="Impacto da rede Guardião"><Mensagem tipo="erro">{erro}</Mensagem></Pagina>;
  }
  if (!dados) {
    return <Pagina titulo="Impacto da rede Guardião"><Carregando texto="Calculando os relatórios…" /></Pagina>;
  }

  const { indicadores } = dados;
  const maiorTipo = Math.max(1, ...dados.tipos.map((t) => t.total));
  const maiorBairro = Math.max(1, ...dados.bairros.map((b) => b.qtTentativas));

  return (
    <Pagina titulo="Impacto da rede Guardião" subtitulo="Relatórios gerados com JOIN e agregação direto no banco Oracle.">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Cartao titulo="Valor protegido" valor={formatarMoeda(indicadores.valorProtegido)} detalhe={`${indicadores.golpesEvitados} golpes evitados`} cor="text-marca-700" />
        <Cartao titulo="Tentativas barradas na porta" valor={String(indicadores.tentativasNaPorta)} detalhe="códigos inválidos ou sem código" cor="text-perigo" />
        <Cartao titulo="Tempo médio de resposta" valor={`${indicadores.minutosMedioResposta.toLocaleString("pt-BR")} min`} detalhe="entre o pedido e a decisão" cor="text-alerta" />
        <Cartao titulo="Idosos protegidos" valor={String(indicadores.idososProtegidos)} detalhe={`${indicadores.idososSemGuardiao} ainda sem guardião`} cor="text-info" />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <Bloco titulo="Pedidos por tipo e decisão">
          <ul className="space-y-3">
            {dados.tipos.map((t) => (
              <li key={t.tipo}>
                <div className="mb-1 flex justify-between text-sm"><span className="font-semibold">{NOMES_TIPO[t.tipo]}</span><span>{t.total}</span></div>
                <div className="flex h-4 overflow-hidden rounded bg-stone-100" role="img"
                  aria-label={`${t.bloqueadas} bloqueados, ${t.liberadas} liberados, ${t.pendentes} pendentes`}>
                  <div className="bg-perigo" style={{ width: `${(t.bloqueadas / maiorTipo) * 100}%` }} />
                  <div className="bg-marca-600" style={{ width: `${(t.liberadas / maiorTipo) * 100}%` }} />
                  <div className="bg-amber-400" style={{ width: `${(t.pendentes / maiorTipo) * 100}%` }} />
                </div>
              </li>
            ))}
          </ul>
          <p className="mt-4 flex flex-wrap gap-4 text-sm text-stone-600">
            <span><span className="inline-block h-3 w-3 rounded-sm bg-perigo" /> bloqueados</span>
            <span><span className="inline-block h-3 w-3 rounded-sm bg-marca-600" /> liberados</span>
            <span><span className="inline-block h-3 w-3 rounded-sm bg-amber-400" /> pendentes</span>
          </p>
        </Bloco>

        <Bloco titulo="Tentativas barradas na porta por bairro">
          {dados.bairros.length === 0 && <p className="text-stone-600">Nenhuma tentativa registrada.</p>}
          <ul className="space-y-3">
            {dados.bairros.map((b) => (
              <li key={b.bairro} className="flex items-center gap-3 text-sm">
                <span className="w-28 shrink-0">{b.bairro}</span>
                <div className="h-4 flex-1 rounded bg-stone-100">
                  <div className="h-4 rounded bg-orange-600" style={{ width: `${(b.qtTentativas / maiorBairro) * 100}%` }} />
                </div>
                <strong className="w-6 text-right">{b.qtTentativas}</strong>
              </li>
            ))}
          </ul>
        </Bloco>

        <Bloco titulo="Valor protegido por idoso">
          <table className="w-full text-left text-sm">
            <thead className="text-xs uppercase text-stone-600"><tr><th className="py-2">Idoso</th><th>Bloqueios</th><th className="text-right">Valor</th></tr></thead>
            <tbody>
              {dados.valor.map((v) => (
                <tr key={v.idoso} className="border-t border-stone-100">
                  <td className="py-2 font-semibold">{v.idoso}</td><td>{v.qtBloqueios}</td>
                  <td className="text-right font-bold text-marca-700">{formatarMoeda(v.valorProtegido)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Bloco>

        <Bloco titulo="Tempo médio de resposta por guardião">
          <table className="w-full text-left text-sm">
            <thead className="text-xs uppercase text-stone-600"><tr><th className="py-2">Guardião</th><th>Respostas</th><th className="text-right">Média</th></tr></thead>
            <tbody>
              {dados.tempos.map((t) => (
                <tr key={t.guardiao} className="border-t border-stone-100">
                  <td className="py-2 font-semibold">{t.guardiao}</td><td>{t.qtRespostas}</td>
                  <td className="text-right">{t.minutosMedios.toLocaleString("pt-BR")} min</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Bloco>
      </div>

      <div className="mt-6">
        <Bloco titulo="Contatos suspeitos recorrentes">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs uppercase text-stone-600">
                <tr><th className="py-2">Contato</th><th>Tipo</th><th>Idosos diferentes</th><th>Bloqueios</th><th>Último uso</th></tr>
              </thead>
              <tbody>
                {dados.contatos.map((c) => (
                  <tr key={c.contato} className="border-t border-stone-100">
                    <td className="py-2 font-mono">{formatarContato(c.contato)}</td>
                    <td>{NOMES_TIPO[c.tipo]}</td><td>{c.qtIdosos}</td>
                    <td><span className="rounded-full bg-red-100 px-2 py-0.5 font-bold text-red-900">{c.qtBloqueios}</span></td>
                    <td>{formatarData(c.ultimoUso)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Bloco>
      </div>

      {dados.semGuardiao.length > 0 && (
        <div className="mt-6">
          <Mensagem tipo="aviso">
            <strong>Idosos sem guardião:</strong> {dados.semGuardiao.map((i) => `${i.nome} (${i.bairro})`).join(", ")}.
            Cadastre um vínculo para que eles possam pedir ajuda.
          </Mensagem>
        </div>
      )}
    </Pagina>
  );
}
