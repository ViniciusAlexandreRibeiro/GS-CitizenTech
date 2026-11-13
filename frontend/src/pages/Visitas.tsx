import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Botao, Campo, CampoSelect, Carregando, ConfirmarExclusao, Etiqueta, Mensagem, Pagina } from "../components/Ui";
import { useUsuario } from "../context/UsuarioContext";
import { api, ApiError, mensagemDeErro } from "../services/api";
import type { AgendadoPor, Prestador, Visita } from "../types";
import { formatarDataHora, paraInputDataHora } from "../utils/formatos";
import { dataHoraNoFuturo, temErros, type Erros } from "../utils/validacoes";
import { Acoes, CampoBusca, Celula, Filtro, FormularioJanela, Principal, Tabela } from "./cadastros/Comum";

interface Formulario {
  idIdoso: string;
  agendadoPor: AgendadoPor;
  idPrestador: string;
  nomeVisitante: string;
  dataAgendada: string;
}

const VAZIO: Formulario = { idIdoso: "", agendadoPor: "EMPRESA", idPrestador: "", nomeVisitante: "", dataAgendada: "" };

export function Visitas() {
  const { idosos } = useUsuario();
  const [prestadores, setPrestadores] = useState<Prestador[]>([]);
  const [visitas, setVisitas] = useState<Visita[] | null>(null);
  const [filtroStatus, setFiltroStatus] = useState("");
  const [busca, setBusca] = useState("");
  const [form, setForm] = useState<Formulario>(VAZIO);
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState<Visita | null>(null);
  const [erros, setErros] = useState<Erros>({});
  const [mensagem, setMensagem] = useState<{ tipo: "erro" | "sucesso"; texto: string } | null>(null);
  const [ultimaCriada, setUltimaCriada] = useState<Visita | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [excluindo, setExcluindo] = useState<Visita | null>(null);

  const carregar = useCallback(async () => {
    try {
      setVisitas(await api.visitas.listar(undefined, filtroStatus || undefined));
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
      setVisitas([]);
    }
  }, [filtroStatus]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  useEffect(() => {
    api.prestadores.listar().then(setPrestadores).catch(() => setPrestadores([]));
  }, []);

  function alterar<K extends keyof Formulario>(campo: K, valor: Formulario[K]) {
    setForm((atual) => ({ ...atual, [campo]: valor }));
  }

  function validar(): Erros {
    const novos: Erros = {};
    if (!form.idIdoso) novos.idIdoso = "Selecione o idoso.";
    if (form.agendadoPor === "EMPRESA" && !form.idPrestador) novos.idPrestador = "Selecione o prestador.";
    if (form.agendadoPor === "GUARDIAO" && !form.idPrestador && form.nomeVisitante.trim().length < 3)
      novos.nomeVisitante = "Informe quem vai visitar.";
    if (!form.dataAgendada) novos.dataAgendada = "Informe a data e a hora.";
    else if (!dataHoraNoFuturo(form.dataAgendada)) novos.dataAgendada = "Escolha um horário no futuro.";
    return novos;
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) return;

    const dados = {
      idIdoso: Number(form.idIdoso),
      agendadoPor: form.agendadoPor,
      idPrestador: form.idPrestador ? Number(form.idPrestador) : null,
      nomeVisitante: form.nomeVisitante.trim(),
      dataAgendada: form.dataAgendada,
    };
    setEnviando(true);
    try {
      if (editando) {
        await api.visitas.atualizar(editando.id, { ...dados, status: "AGENDADA" });
        setMensagem({ tipo: "sucesso", texto: "Visita remarcada. O código continua o mesmo." });
        setUltimaCriada(null);
      } else {
        setUltimaCriada(await api.visitas.agendar(dados));
        setMensagem(null);
      }
      setAberto(false);
      await carregar();
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      else setErros({ geral: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }

  function abrir(v: Visita | null) {
    setEditando(v);
    setErros({});
    setForm(v ? {
      idIdoso: String(v.idIdoso),
      agendadoPor: v.agendadoPor,
      idPrestador: v.idPrestador ? String(v.idPrestador) : "",
      nomeVisitante: v.idPrestador ? "" : v.visitante,
      dataAgendada: v.dataAgendada.slice(0, 16),
    } : VAZIO);
    setAberto(true);
  }

  async function cancelarVisita(v: Visita) {
    setMensagem(null);
    try {
      await api.visitas.atualizar(v.id, {
        idIdoso: v.idIdoso, idPrestador: v.idPrestador, nomeVisitante: v.visitante,
        dataAgendada: v.dataAgendada, agendadoPor: v.agendadoPor, status: "CANCELADA",
      });
      setMensagem({ tipo: "sucesso", texto: `Visita ${v.codigo} cancelada. O código não vale mais.` });
      await carregar();
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    }
  }

  async function excluir() {
    if (!excluindo) return;
    try {
      await api.visitas.remover(excluindo.id);
      setMensagem({ tipo: "sucesso", texto: "Visita excluída." });
      await carregar();
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    } finally {
      setExcluindo(null);
    }
  }

  const termo = busca.trim().toLowerCase();
  const lista = (visitas ?? []).filter(
    (v) => !termo || v.nomeIdoso.toLowerCase().includes(termo) || v.codigo.includes(termo) || v.visitante.toLowerCase().includes(termo),
  );

  return (
    <Pagina titulo="Visitas" subtitulo="Agende antes de ir. O visitante informa o código e o idoso confere na porta."
      acoes={<Botao onClick={() => abrir(null)}>+ Agendar visita</Botao>}>
      {mensagem && <Mensagem tipo={mensagem.tipo}>{mensagem.texto}</Mensagem>}

      {ultimaCriada && (
        <div className="mb-6 flex flex-col gap-4 rounded-2xl border border-marca-600/30 bg-marca-50 p-5 sm:flex-row sm:items-center sm:justify-between" aria-live="polite">
          <div>
            <p className="font-semibold text-marca-800">Visita agendada! Entregue este código ao visitante:</p>
            <p className="text-stone-700">{ultimaCriada.visitante} · {ultimaCriada.nomeIdoso} · {formatarDataHora(ultimaCriada.dataAgendada)}</p>
          </div>
          <p className="font-mono text-4xl font-black tracking-[0.25em] text-marca-900">{ultimaCriada.codigo}</p>
        </div>
      )}

      <div className="mb-4 flex flex-col gap-3 sm:flex-row">
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar por idoso, visitante ou código…" />
        <Filtro valor={filtroStatus} aoMudar={setFiltroStatus} rotulo="Filtrar por status">
          <option value="">Todos os status</option>
          <option value="AGENDADA">Agendadas</option>
          <option value="CONFIRMADA">Confirmadas</option>
          <option value="CANCELADA">Canceladas</option>
        </Filtro>
      </div>

      {visitas === null ? <Carregando /> : (
        <Tabela colunas={["Quando", "Idoso", "Visitante", "Código", "Status"]} vazio={lista.length === 0}>
          {lista.map((v) => (
            <tr key={v.id}>
              <Celula className="whitespace-nowrap">{formatarDataHora(v.dataAgendada)}</Celula>
              <Principal titulo={v.nomeIdoso} detalhe={v.bairro} />
              <Principal titulo={v.visitante} detalhe={v.empresa ?? "Agendado pela família"} />
              <Celula className="font-mono font-bold tracking-wider">{v.codigo}</Celula>
              <Celula><Etiqueta status={v.status} /></Celula>
              <Acoes
                aoEditar={v.status === "AGENDADA" ? () => abrir(v) : undefined}
                textoEditar="Remarcar"
                aoExcluir={() => setExcluindo(v)}
                extra={v.status === "AGENDADA" && (
                  <button className="text-stone-500 hover:text-amber-800 hover:underline" onClick={() => cancelarVisita(v)}>Cancelar</button>
                )}
              />
            </tr>
          ))}
        </Tabela>
      )}

      <FormularioJanela titulo={editando ? `Remarcar visita ${editando.codigo}` : "Agendar visita"} aberta={aberto}
        aoFechar={() => setAberto(false)} onSubmit={salvar} enviando={enviando}
        textoSalvar={editando ? "Salvar nova data" : "Agendar e gerar código"}>
        {erros.geral && <Mensagem tipo="erro">{erros.geral}</Mensagem>}
        <CampoSelect id="idIdoso" rotulo="Idoso" value={form.idIdoso} onChange={(v) => alterar("idIdoso", v)} erro={erros.idIdoso}>
          <option value="">Selecione…</option>
          {idosos.map((i) => <option key={i.id} value={i.id}>{i.nome} · {i.bairro}</option>)}
        </CampoSelect>

        <fieldset className="mb-4">
          <legend className="mb-1.5 text-sm font-semibold text-stone-700">Quem está agendando?</legend>
          <div className="grid grid-cols-2 gap-1 rounded-xl bg-stone-100 p-1">
            {(["EMPRESA", "GUARDIAO"] as AgendadoPor[]).map((opcao) => (
              <label key={opcao}
                className={`flex min-h-11 cursor-pointer items-center justify-center rounded-lg font-semibold ${form.agendadoPor === opcao ? "bg-white shadow-sm" : "text-stone-600"}`}>
                <input type="radio" name="agendadoPor" className="sr-only" checked={form.agendadoPor === opcao}
                  onChange={() => alterar("agendadoPor", opcao)} />
                {opcao === "EMPRESA" ? "Empresa" : "Família"}
              </label>
            ))}
          </div>
        </fieldset>

        {form.agendadoPor === "EMPRESA" ? (
          <CampoSelect id="idPrestador" rotulo="Prestador" value={form.idPrestador} onChange={(v) => alterar("idPrestador", v)} erro={erros.idPrestador}>
            <option value="">Selecione…</option>
            {prestadores.map((p) => <option key={p.id} value={p.id}>{p.nome} · {p.nomeEmpresa}</option>)}
          </CampoSelect>
        ) : (
          <Campo id="nomeVisitante" rotulo="Quem vai visitar?" placeholder="Ex.: Antônio (pedreiro)" maxLength={80}
            value={form.nomeVisitante} onChange={(e) => { alterar("nomeVisitante", e.target.value); alterar("idPrestador", ""); }}
            erro={erros.nomeVisitante} />
        )}

        <Campo id="dataAgendada" rotulo="Data e hora" type="datetime-local" min={paraInputDataHora(new Date())}
          value={form.dataAgendada} onChange={(e) => alterar("dataAgendada", e.target.value)} erro={erros.dataAgendada}
          dica="O código vale de 2 horas antes até 2 horas depois deste horário." />
      </FormularioJanela>

      {excluindo && (
        <ConfirmarExclusao
          texto={`Excluir a visita ${excluindo.codigo} de ${excluindo.nomeIdoso}?`}
          aoConfirmar={excluir}
          aoCancelar={() => setExcluindo(null)}
        />
      )}
    </Pagina>
  );
}
