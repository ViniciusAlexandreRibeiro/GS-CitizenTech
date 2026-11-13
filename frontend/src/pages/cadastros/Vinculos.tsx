import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Campo, CampoSelect, ConfirmarExclusao, Mensagem } from "../../components/Ui";
import { useUsuario } from "../../context/UsuarioContext";
import { api, ApiError, mensagemDeErro } from "../../services/api";
import type { Vinculo } from "../../types";
import { mascaraTelefone } from "../../utils/formatos";
import { temErros, type Erros } from "../../utils/validacoes";
import { Acoes, BarraLista, Celula, Filtro, FormularioJanela, Principal, Tabela } from "./Comum";

const VAZIO = { idIdoso: "", idGuardiao: "", parentesco: "", prioridade: "1" };

export function CadastroVinculos() {
  const { idosos, guardioes, recarregarPessoas } = useUsuario();
  const [vinculos, setVinculos] = useState<Vinculo[]>([]);
  const [filtroIdoso, setFiltroIdoso] = useState("");
  const [form, setForm] = useState(VAZIO);
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState<Vinculo | null>(null);
  const [erros, setErros] = useState<Erros>({});
  const [mensagem, setMensagem] = useState<{ tipo: "erro" | "sucesso"; texto: string } | null>(null);
  const [excluindo, setExcluindo] = useState<Vinculo | null>(null);
  const [enviando, setEnviando] = useState(false);

  const carregar = useCallback(async () => {
    try {
      setVinculos(await api.vinculos.listar(filtroIdoso ? Number(filtroIdoso) : undefined));
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    }
  }, [filtroIdoso]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function validar(): Erros {
    const e: Erros = {};
    if (!form.idIdoso) e.idIdoso = "Selecione o idoso.";
    if (!form.idGuardiao) e.idGuardiao = "Selecione o guardião.";
    if (form.parentesco.trim().length < 2) e.parentesco = "Informe o parentesco (ex.: Filha, Vizinho).";
    const prioridade = Number(form.prioridade);
    if (!(prioridade >= 1 && prioridade <= 5)) e.prioridade = "A prioridade vai de 1 a 5.";
    return e;
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) return;
    const dados = {
      idIdoso: Number(form.idIdoso),
      idGuardiao: Number(form.idGuardiao),
      parentesco: form.parentesco.trim(),
      prioridade: Number(form.prioridade),
    };
    setEnviando(true);
    try {
      if (editando) await api.vinculos.atualizar(editando.idIdoso, editando.idGuardiao, dados);
      else await api.vinculos.cadastrar(dados);
      setMensagem({ tipo: "sucesso", texto: editando ? "Vínculo atualizado." : "Guardião vinculado ao idoso." });
      setAberto(false);
      await Promise.all([carregar(), recarregarPessoas()]);
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      else setErros({ geral: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }

  function abrir(v: Vinculo | null) {
    setEditando(v);
    setErros({});
    setForm(v
      ? { idIdoso: String(v.idIdoso), idGuardiao: String(v.idGuardiao), parentesco: v.parentesco, prioridade: String(v.prioridade) }
      : { ...VAZIO, idIdoso: filtroIdoso });
    setAberto(true);
  }

  async function excluir() {
    if (!excluindo) return;
    try {
      await api.vinculos.remover(excluindo.idIdoso, excluindo.idGuardiao);
      setMensagem({ tipo: "sucesso", texto: "Vínculo removido." });
      await Promise.all([carregar(), recarregarPessoas()]);
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    } finally {
      setExcluindo(null);
    }
  }

  return (
    <>
      {mensagem && <Mensagem tipo={mensagem.tipo}>{mensagem.texto}</Mensagem>}
      <BarraLista textoNovo="Vincular guardião" aoNovo={() => abrir(null)}>
        <Filtro valor={filtroIdoso} aoMudar={setFiltroIdoso} rotulo="Filtrar por idoso">
          <option value="">Todos os idosos</option>
          {idosos.map((i) => <option key={i.id} value={i.id}>{i.nome}</option>)}
        </Filtro>
      </BarraLista>
      <Tabela colunas={["Idoso", "Guardião", "Prioridade"]} vazio={vinculos.length === 0}>
        {vinculos.map((v) => (
          <tr key={`${v.idIdoso}-${v.idGuardiao}`}>
            <Principal titulo={v.nomeIdoso} />
            <Principal titulo={v.nomeGuardiao} detalhe={`${v.parentesco} · ${mascaraTelefone(v.telefoneGuardiao)}`} />
            <Celula>{v.prioridade}º a ser avisado</Celula>
            <Acoes aoEditar={() => abrir(v)} aoExcluir={() => setExcluindo(v)} />
          </tr>
        ))}
      </Tabela>

      <FormularioJanela titulo={editando ? "Editar vínculo" : "Vincular guardião"} aberta={aberto}
        aoFechar={() => setAberto(false)} onSubmit={salvar} enviando={enviando}>
        {erros.geral && <Mensagem tipo="erro">{erros.geral}</Mensagem>}
        {editando ? (
          <p className="mb-4 rounded-xl bg-stone-100 p-3 text-sm">
            <strong>{editando.nomeGuardiao}</strong> cuida de <strong>{editando.nomeIdoso}</strong>.
            Para trocar as pessoas, exclua e crie outro vínculo.
          </p>
        ) : (
          <>
            <CampoSelect id="vinculo-idoso" rotulo="Idoso" value={form.idIdoso} erro={erros.idIdoso}
              onChange={(v) => setForm({ ...form, idIdoso: v })}>
              <option value="">Selecione…</option>
              {idosos.map((i) => <option key={i.id} value={i.id}>{i.nome}</option>)}
            </CampoSelect>
            <CampoSelect id="vinculo-guardiao" rotulo="Guardião" value={form.idGuardiao} erro={erros.idGuardiao}
              onChange={(v) => setForm({ ...form, idGuardiao: v })}>
              <option value="">Selecione…</option>
              {guardioes.map((g) => <option key={g.id} value={g.id}>{g.nome}</option>)}
            </CampoSelect>
          </>
        )}
        <div className="grid gap-x-3 sm:grid-cols-2">
          <Campo id="vinculo-parentesco" rotulo="Parentesco" value={form.parentesco} maxLength={30} placeholder="Filha, neto, vizinho…"
            onChange={(e) => setForm({ ...form, parentesco: e.target.value })} erro={erros.parentesco} />
          <Campo id="vinculo-prioridade" rotulo="Prioridade (1 a 5)" type="number" min={1} max={5}
            value={form.prioridade} onChange={(e) => setForm({ ...form, prioridade: e.target.value })} erro={erros.prioridade}
            dica="1 = avisado primeiro" />
        </div>
      </FormularioJanela>

      {excluindo && (
        <ConfirmarExclusao texto={`${excluindo.nomeGuardiao} deixa de ser guardião de ${excluindo.nomeIdoso}. Confirmar?`}
          aoConfirmar={excluir} aoCancelar={() => setExcluindo(null)} />
      )}
    </>
  );
}
