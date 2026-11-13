import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { Icone } from "../components/Icone";
import { ConfirmarExclusao, Etiqueta, Mensagem, Pagina } from "../components/Ui";
import { useUsuario } from "../context/UsuarioContext";
import { api, mensagemDeErro } from "../services/api";
import type { Solicitacao } from "../types";
import { formatarMoeda, tempoRelativo } from "../utils/formatos";

const BOTOES = [
  { para: "/porta", icone: "porta", titulo: "Tem alguém na porta", texto: "Confira antes de abrir", cor: "bg-marca-600 hover:bg-marca-700" },
  { para: "/pedido?tipo=PIX", icone: "dinheiro", titulo: "Pediram dinheiro", texto: "Pix, boleto, transferência", cor: "bg-alerta hover:bg-amber-800" },
  { para: "/pedido?tipo=LIGACAO", icone: "mensagem", titulo: "Recebi algo estranho", texto: "Ligação, link ou mensagem", cor: "bg-info hover:bg-blue-950" },
] as const;

export function Inicio() {
  const { perfil } = useUsuario();
  const navegar = useNavigate();
  const [pedidos, setPedidos] = useState<Solicitacao[]>([]);
  const [erro, setErro] = useState("");
  const [excluindo, setExcluindo] = useState<Solicitacao | null>(null);
  const [ocupado, setOcupado] = useState(false);

  const idIdoso = perfil?.tipo === "idoso" ? perfil.id : null;

  useEffect(() => {
    if (idIdoso === null) return;
    api.solicitacoes
      .listar({ idoso: idIdoso })
      .then(setPedidos)
      .catch((e) => setErro(mensagemDeErro(e)));
  }, [idIdoso]);

  async function excluir() {
    if (!excluindo) return;
    setOcupado(true);
    try {
      await api.solicitacoes.remover(excluindo.id);
      setPedidos((lista) => lista.filter((p) => p.id !== excluindo.id));
      setExcluindo(null);
    } catch (e) {
      setErro(mensagemDeErro(e));
      setExcluindo(null);
    } finally {
      setOcupado(false);
    }
  }

  if (!perfil || perfil.tipo !== "idoso") {
    return <BoasVindas />;
  }

  const respondidos = pedidos.filter((p) => p.status !== "PENDENTE").slice(0, 3);
  const pendentes = pedidos.filter((p) => p.status === "PENDENTE");

  return (
    <Pagina titulo={`Olá, ${perfil.nome.split(" ")[0]}.`} tituloAba="Início" subtitulo="Precisa de ajuda? Escolha o que está acontecendo.">
      <section aria-label="O que está acontecendo?" className="grid gap-4 md:grid-cols-3">
        {BOTOES.map((b) => (
          <Link key={b.para} to={b.para}
            className={`flex min-h-28 items-center gap-4 rounded-2xl p-5 text-white shadow-sm ${b.cor}`}>
            <span className="rounded-xl bg-white/20 p-3"><Icone nome={b.icone} tamanho={32} /></span>
            <span>
              <span className="block text-xl font-extrabold leading-tight">{b.titulo}</span>
              <span className="block text-white/90">{b.texto}</span>
            </span>
          </Link>
        ))}
      </section>

      {erro && <div className="mt-6"><Mensagem tipo="erro">{erro}</Mensagem></div>}

      <section className="mt-10">
        <h2 className="mb-3 text-lg font-bold">Respostas dos seus guardiões</h2>
        {respondidos.length === 0 && <p className="text-stone-600">Nenhuma resposta ainda.</p>}
        <ul className="grid gap-3 md:grid-cols-3">
          {respondidos.map((p) => (
            <li key={p.id} className="rounded-2xl border border-stone-200 bg-white p-4">
              <div className="flex items-center justify-between gap-2">
                <Etiqueta status={p.status} />
                <span className="text-sm text-stone-500">{tempoRelativo(p.dataResposta ?? p.dataAbertura)}</span>
              </div>
              <p className="mt-2 font-semibold">{p.descricao}</p>
              {p.orientacao && (
                <p className="mt-3 rounded-xl bg-stone-100 p-3 text-[0.95rem]">
                  <strong>{p.nomeGuardiaoResposta}:</strong> “{p.orientacao}”
                </p>
              )}
              {p.status === "BLOQUEADA" && p.valor && (
                <p className="mt-2 text-sm font-bold text-marca-700">{formatarMoeda(p.valor)} protegidos</p>
              )}
            </li>
          ))}
        </ul>
      </section>

      {pendentes.length > 0 && (
        <section className="mt-10">
          <h2 className="mb-3 text-lg font-bold">Aguardando resposta</h2>
          <ul className="grid gap-3 md:grid-cols-2">
            {pendentes.map((p) => (
              <li key={p.id} className="rounded-2xl border border-amber-200 bg-amber-50/40 p-4">
                <div className="flex items-center justify-between">
                  <Etiqueta status={p.status} />
                  <span className="text-sm text-stone-500">{tempoRelativo(p.dataAbertura)}</span>
                </div>
                <p className="mt-2 font-semibold">{p.descricao}</p>
                <p className="mt-1 text-sm text-stone-600">Não siga em frente enquanto ninguém responder.</p>
                <div className="mt-3 flex gap-4">
                  {p.tipo !== "VISITA" && (
                    <button className="font-bold text-marca-700 underline" onClick={() => navegar(`/pedido?editar=${p.id}`)}>
                      Corrigir
                    </button>
                  )}
                  <button className="font-bold text-red-700 underline" onClick={() => setExcluindo(p)}>Excluir</button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      {excluindo && (
        <ConfirmarExclusao
          texto="Excluir este pedido de ajuda? Seus guardiões não verão mais o pedido."
          aoConfirmar={excluir}
          aoCancelar={() => setExcluindo(null)}
          ocupado={ocupado}
        />
      )}
    </Pagina>
  );
}

/** Tela de entrada: explica o Guardião e deixa escolher quem está usando (substitui o login no MVP). */
function BoasVindas() {
  const { idosos, guardioes, escolherPerfil, erroApi } = useUsuario();
  const navegar = useNavigate();

  const botao = "flex min-h-12 w-full items-center justify-between rounded-xl px-4 text-left font-semibold hover:bg-stone-100";

  return (
    <Pagina titulo="Quem está usando?" tituloAba="Início"
      subtitulo="Ninguém deveria decidir sozinho se abre a porta ou faz um Pix. Escolha seu perfil para começar.">
      {erroApi ? (
        <p className="text-stone-600">Não foi possível carregar as pessoas cadastradas.</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-3">
          <section className="rounded-2xl border border-stone-200 bg-white p-5">
            <h2 className="flex items-center gap-2 text-lg font-bold"><Icone nome="porta" className="text-marca-600" /> Sou idoso</h2>
            <p className="mb-3 mt-1 text-sm text-stone-600">Confira quem está na porta e peça ajuda antes de pagar.</p>
            <ul>
              {idosos.map((i) => (
                <li key={i.id}>
                  <button className={botao} onClick={() => escolherPerfil({ tipo: "idoso", id: i.id, nome: i.nome })}>
                    {i.nome} <span aria-hidden="true" className="text-stone-400">›</span>
                  </button>
                </li>
              ))}
            </ul>
          </section>
          <section className="rounded-2xl border border-stone-200 bg-white p-5">
            <h2 className="flex items-center gap-2 text-lg font-bold"><Icone nome="escudo" className="text-marca-600" /> Sou guardião</h2>
            <p className="mb-3 mt-1 text-sm text-stone-600">Responda aos pedidos de ajuda da sua família.</p>
            <ul>
              {guardioes.map((g) => (
                <li key={g.id}>
                  <button className={botao}
                    onClick={() => { escolherPerfil({ tipo: "guardiao", id: g.id, nome: g.nome }); navegar("/guardiao"); }}>
                    {g.nome} <span aria-hidden="true" className="text-stone-400">›</span>
                  </button>
                </li>
              ))}
            </ul>
          </section>
          <section className="rounded-2xl border border-stone-200 bg-white p-5">
            <h2 className="flex items-center gap-2 text-lg font-bold"><Icone nome="relogio" className="text-marca-600" /> Empresa ou gestão</h2>
            <p className="mb-3 mt-1 text-sm text-stone-600">Agende visitas, mantenha os cadastros e acompanhe o impacto.</p>
            <ul>
              {[["/visitas", "Agendar visitas"], ["/cadastros", "Cadastros"], ["/impacto", "Ver o impacto"]].map(([para, texto]) => (
                <li key={para}>
                  <button className={botao} onClick={() => { escolherPerfil({ tipo: "admin" }); navegar(para); }}>
                    {texto} <span aria-hidden="true" className="text-stone-400">›</span>
                  </button>
                </li>
              ))}
            </ul>
          </section>
        </div>
      )}
    </Pagina>
  );
}
