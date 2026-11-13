import { useCallback, useEffect, useState } from "react";
import { Botao, Carregando, ConfirmarExclusao, EscolhaPerfil, Etiqueta, Mensagem, Pagina } from "../components/Ui";
import { useUsuario } from "../context/UsuarioContext";
import { api, mensagemDeErro } from "../services/api";
import type { Solicitacao, Vinculo } from "../types";
import { formatarContato, formatarDataHora, formatarMoeda, NOMES_TIPO, tempoRelativo } from "../utils/formatos";

function CartaoPendente({ pedido, idGuardiao, aoResponder }: {
  pedido: Solicitacao;
  idGuardiao: number;
  aoResponder: () => void;
}) {
  const [orientacao, setOrientacao] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function responder(decisao: "LIBERAR" | "BLOQUEAR") {
    setErro("");
    if (decisao === "BLOQUEAR" && orientacao.trim().length < 3) {
      setErro("Escreva uma orientação para o idoso antes de bloquear.");
      return;
    }
    setEnviando(true);
    try {
      await api.solicitacoes.responder(pedido.id, idGuardiao, decisao, orientacao.trim());
      aoResponder();
    } catch (e) {
      setErro(mensagemDeErro(e));
      setEnviando(false);
    }
  }

  const idCampo = `orientacao-${pedido.id}`;
  const contatoPerigoso = (pedido.bloqueiosDoContato ?? 0) > 0;
  const detalhes = [
    pedido.valor != null ? formatarMoeda(pedido.valor) : null,
    pedido.contatoSuspeito ? formatarContato(pedido.contatoSuspeito) : null,
  ].filter(Boolean).join(" · ");

  return (
    <article className="rounded-2xl border border-stone-200 bg-white p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-alerta">{NOMES_TIPO[pedido.tipo]}</p>
          <h3 className="text-lg font-bold">{pedido.nomeIdoso}</h3>
        </div>
        <span className="whitespace-nowrap text-sm text-stone-500">{tempoRelativo(pedido.dataAbertura)}</span>
      </div>
      <p className="mt-2 text-stone-800">“{pedido.descricao}”</p>
      {detalhes && <p className="mt-1 text-sm text-stone-500">{detalhes}</p>}
      {(contatoPerigoso || pedido.aprovacaoObrigatoria) && (
        <ul className="mt-3 space-y-1 text-sm font-semibold">
          {contatoPerigoso && <li className="text-red-700">⚠ Este contato já foi bloqueado {pedido.bloqueiosDoContato} vez(es) por outros guardiões</li>}
          {pedido.aprovacaoObrigatoria && <li className="text-amber-800">⚠ Valor acima do limite de {formatarMoeda(pedido.limitePix)}</li>}
        </ul>
      )}
      <label htmlFor={idCampo} className="sr-only">Orientação para {pedido.nomeIdoso.split(" ")[0]}</label>
      <textarea id={idCampo} rows={2} value={orientacao} onChange={(e) => setOrientacao(e.target.value)} maxLength={400}
        placeholder={`Escreva uma orientação para ${pedido.nomeIdoso.split(" ")[0]}…`}
        className="mt-4 w-full rounded-xl border border-stone-300 p-3" />
      {erro && <div className="mt-2"><Mensagem tipo="erro">{erro}</Mensagem></div>}
      <div className="mt-3 grid grid-cols-2 gap-2">
        <Botao variante="perigo" className="px-3" onClick={() => responder("BLOQUEAR")} disabled={enviando}>Bloquear</Botao>
        <Botao className="px-3" onClick={() => responder("LIBERAR")} disabled={enviando}>Pode seguir</Botao>
      </div>
    </article>
  );
}

export function PainelGuardiao() {
  const { perfil } = useUsuario();
  const [pedidos, setPedidos] = useState<Solicitacao[] | null>(null);
  const [cuidados, setCuidados] = useState<Vinculo[]>([]);
  const [filtro, setFiltro] = useState("");
  const [erro, setErro] = useState("");
  const [excluindo, setExcluindo] = useState<Solicitacao | null>(null);
  const [ocupado, setOcupado] = useState(false);

  const idGuardiao = perfil?.tipo === "guardiao" ? perfil.id : null;

  const carregar = useCallback(async () => {
    if (idGuardiao === null) return;
    try {
      const [lista, vinculos] = await Promise.all([
        api.solicitacoes.listar({ guardiao: idGuardiao }),
        api.vinculos.listar(undefined, idGuardiao),
      ]);
      setPedidos(lista);
      setCuidados(vinculos);
      setErro("");
    } catch (e) {
      setErro(mensagemDeErro(e));
      setPedidos([]);
    }
  }, [idGuardiao]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  if (idGuardiao === null) {
    return (
      <Pagina titulo="Painel do guardião">
        <EscolhaPerfil tipo="guardiao" />
      </Pagina>
    );
  }

  async function excluir() {
    if (!excluindo) return;
    setOcupado(true);
    try {
      await api.solicitacoes.remover(excluindo.id);
      setExcluindo(null);
      await carregar();
    } catch (e) {
      setErro(mensagemDeErro(e));
      setExcluindo(null);
    } finally {
      setOcupado(false);
    }
  }

  const pendentes = pedidos?.filter((p) => p.status === "PENDENTE") ?? [];
  const historico = (pedidos?.filter((p) => p.status !== "PENDENTE") ?? []).filter((p) => !filtro || p.status === filtro);
  const protegido = (pedidos ?? []).filter((p) => p.status === "BLOQUEADA").reduce((soma, p) => soma + (p.valor ?? 0), 0);

  return (
    <Pagina
      titulo="Painel do guardião"
      subtitulo={cuidados.length ? `Você cuida de: ${cuidados.map((c) => `${c.nomeIdoso} (${c.parentesco.toLowerCase()})`).join(" · ")}` : undefined}
    >
      <div className="mb-8 grid grid-cols-2 gap-3 sm:max-w-md">
        <div className="rounded-2xl border border-stone-200 bg-white p-4">
          <p className="text-sm text-stone-500">Pendentes</p>
          <p className="text-xl font-extrabold text-alerta sm:text-2xl">{pendentes.length}</p>
        </div>
        <div className="rounded-2xl border border-stone-200 bg-white p-4">
          <p className="text-sm text-stone-500">Protegido</p>
          <p className="text-xl font-extrabold text-marca-700 sm:text-2xl">{formatarMoeda(protegido)}</p>
        </div>
      </div>

      {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
      {pedidos === null && <Carregando />}

      <div className="grid gap-8 lg:grid-cols-[1.2fr_1fr]">
        <section aria-labelledby="titulo-pendentes">
          <h2 id="titulo-pendentes" className="mb-3 text-lg font-bold">Precisam da sua resposta</h2>
          {pedidos !== null && pendentes.length === 0 && (
            <p className="rounded-2xl border border-dashed border-stone-300 p-6 text-center text-stone-500">Nenhum pedido pendente. Tudo certo por aqui.</p>
          )}
          <div className="space-y-4">
            {pendentes.map((p) => (
              <CartaoPendente key={p.id} pedido={p} idGuardiao={idGuardiao} aoResponder={carregar} />
            ))}
          </div>
        </section>

        <section aria-labelledby="titulo-historico">
          <div className="mb-3 flex items-center justify-between gap-2">
            <h2 id="titulo-historico" className="text-lg font-bold">Histórico</h2>
            <label className="text-sm">
              <span className="sr-only">Filtrar por decisão</span>
              <select value={filtro} onChange={(e) => setFiltro(e.target.value)} className="min-h-11 rounded-xl border border-stone-300 bg-white px-2">
                <option value="">Todas</option>
                <option value="BLOQUEADA">Bloqueadas</option>
                <option value="LIBERADA">Liberadas</option>
              </select>
            </label>
          </div>
          <ul className="divide-y divide-stone-100 rounded-2xl border border-stone-200 bg-white">
            {historico.map((p) => (
              <li key={p.id} className="flex items-start justify-between gap-3 px-4 py-3">
                <div className="min-w-0">
                  <p className="font-semibold">{p.nomeIdoso}</p>
                  <p className="text-sm text-stone-500">
                    {NOMES_TIPO[p.tipo]}{p.valor ? ` · ${formatarMoeda(p.valor)}` : ""} · {formatarDataHora(p.dataAbertura)}
                  </p>
                </div>
                <div className="flex shrink-0 flex-col items-end gap-1">
                  <Etiqueta status={p.status} />
                  <button className="text-xs font-semibold text-stone-500 hover:text-red-700 hover:underline" onClick={() => setExcluindo(p)}>Excluir</button>
                </div>
              </li>
            ))}
            {historico.length === 0 && <li className="px-4 py-6 text-center text-stone-500">Nada por aqui ainda.</li>}
          </ul>
        </section>
      </div>

      {excluindo && (
        <ConfirmarExclusao
          texto={`Excluir do histórico o pedido "${excluindo.descricao}"?`}
          aoConfirmar={excluir}
          aoCancelar={() => setExcluindo(null)}
          ocupado={ocupado}
        />
      )}
    </Pagina>
  );
}
