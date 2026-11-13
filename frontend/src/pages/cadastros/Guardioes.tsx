import { useState, type FormEvent } from "react";
import { Campo, ConfirmarExclusao, Mensagem } from "../../components/Ui";
import { useUsuario } from "../../context/UsuarioContext";
import { api, ApiError, mensagemDeErro } from "../../services/api";
import type { Guardiao } from "../../types";
import { mascaraCpf, mascaraTelefone } from "../../utils/formatos";
import { cpfValido, emailValido, telefoneValido, temErros, type Erros } from "../../utils/validacoes";
import { Acoes, BarraLista, CampoBusca, Celula, FormularioJanela, Principal, Tabela } from "./Comum";

const VAZIO = { nome: "", cpf: "", telefone: "", email: "" };

export function CadastroGuardioes() {
  const { guardioes, recarregarPessoas } = useUsuario();
  const [form, setForm] = useState(VAZIO);
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState<Guardiao | null>(null);
  const [erros, setErros] = useState<Erros>({});
  const [mensagem, setMensagem] = useState<{ tipo: "erro" | "sucesso"; texto: string } | null>(null);
  const [busca, setBusca] = useState("");
  const [excluindo, setExcluindo] = useState<Guardiao | null>(null);
  const [enviando, setEnviando] = useState(false);

  const alterar = (campo: keyof typeof VAZIO, valor: string) => setForm((f) => ({ ...f, [campo]: valor }));

  function validar(): Erros {
    const e: Erros = {};
    if (form.nome.trim().length < 3) e.nome = "Informe o nome completo.";
    if (!cpfValido(form.cpf)) e.cpf = editando ? "Digite o CPF completo para confirmar a alteração." : "CPF inválido: confira os dígitos.";
    if (!telefoneValido(form.telefone)) e.telefone = "Informe o telefone com DDD.";
    if (!emailValido(form.email)) e.email = "E-mail inválido.";
    return e;
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) return;
    setEnviando(true);
    try {
      if (editando) await api.guardioes.atualizar(editando.id, form);
      else await api.guardioes.cadastrar(form);
      setMensagem({ tipo: "sucesso", texto: editando ? "Guardião atualizado." : "Guardião cadastrado." });
      setAberto(false);
      await recarregarPessoas();
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      else setErros({ geral: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }

  function abrir(g: Guardiao | null) {
    setEditando(g);
    setErros({});
    setForm(g ? { nome: g.nome, cpf: "", telefone: mascaraTelefone(g.telefone), email: g.email } : VAZIO);
    setAberto(true);
  }

  async function excluir() {
    if (!excluindo) return;
    try {
      await api.guardioes.remover(excluindo.id);
      setMensagem({ tipo: "sucesso", texto: "Guardião excluído. O histórico das respostas dele foi mantido." });
      await recarregarPessoas();
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    } finally {
      setExcluindo(null);
    }
  }

  const lista = guardioes.filter((g) => g.nome.toLowerCase().includes(busca.toLowerCase()));

  return (
    <>
      {mensagem && <Mensagem tipo={mensagem.tipo}>{mensagem.texto}</Mensagem>}
      <BarraLista textoNovo="Novo guardião" aoNovo={() => abrir(null)}>
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar guardião…" />
      </BarraLista>
      <Tabela colunas={["Nome", "Telefone"]} vazio={lista.length === 0}>
        {lista.map((g) => (
          <tr key={g.id}>
            <Principal titulo={g.nome} detalhe={g.email} />
            <Celula className="whitespace-nowrap">{mascaraTelefone(g.telefone)}</Celula>
            <Acoes aoEditar={() => abrir(g)} aoExcluir={() => setExcluindo(g)} />
          </tr>
        ))}
      </Tabela>

      <FormularioJanela titulo={editando ? `Editar ${editando.nome}` : "Novo guardião"} aberta={aberto}
        aoFechar={() => setAberto(false)} onSubmit={salvar} enviando={enviando}>
        {erros.geral && <Mensagem tipo="erro">{erros.geral}</Mensagem>}
        <Campo id="guardiao-nome" rotulo="Nome completo" value={form.nome} maxLength={80} autoFocus
          onChange={(e) => alterar("nome", e.target.value)} erro={erros.nome} />
        <div className="grid gap-x-3 sm:grid-cols-2">
          <Campo id="guardiao-cpf" rotulo="CPF" inputMode="numeric" value={form.cpf}
            placeholder={editando ? editando.cpfMascarado : "000.000.000-00"}
            onChange={(e) => alterar("cpf", mascaraCpf(e.target.value))} erro={erros.cpf} />
          <Campo id="guardiao-telefone" rotulo="Telefone" inputMode="tel" value={form.telefone}
            onChange={(e) => alterar("telefone", mascaraTelefone(e.target.value))} erro={erros.telefone} />
        </div>
        <Campo id="guardiao-email" rotulo="E-mail" type="email" value={form.email} maxLength={100}
          onChange={(e) => alterar("email", e.target.value)} erro={erros.email} />
      </FormularioJanela>

      {excluindo && (
        <ConfirmarExclusao texto={`Excluir ${excluindo.nome}? Ele deixa de ser guardião de todos os idosos.`}
          aoConfirmar={excluir} aoCancelar={() => setExcluindo(null)} />
      )}
    </>
  );
}
