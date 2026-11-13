import { useEffect, useState, type FormEvent } from "react";
import { Campo, ConfirmarExclusao, Mensagem } from "../../components/Ui";
import { useUsuario } from "../../context/UsuarioContext";
import { api, ApiError, mensagemDeErro } from "../../services/api";
import type { Idoso } from "../../types";
import { formatarData, formatarMoeda, mascaraCpf, mascaraTelefone } from "../../utils/formatos";
import { cpfValido, idadeEmAnos, telefoneValido, temErros, type Erros } from "../../utils/validacoes";
import { Acoes, BarraLista, CampoBusca, Celula, FormularioJanela, Principal, Tabela } from "./Comum";

const VAZIO = { nome: "", cpf: "", telefone: "", bairro: "", dataNascimento: "", limitePix: "500" };

export function CadastroIdosos() {
  const { idosos, recarregarPessoas } = useUsuario();
  const [form, setForm] = useState(VAZIO);
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState<Idoso | null>(null);
  const [erros, setErros] = useState<Erros>({});
  const [mensagem, setMensagem] = useState<{ tipo: "erro" | "sucesso"; texto: string } | null>(null);
  const [busca, setBusca] = useState("");
  const [excluindo, setExcluindo] = useState<Idoso | null>(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    recarregarPessoas();
  }, [recarregarPessoas]);

  const alterar = (campo: keyof typeof VAZIO, valor: string) => setForm((f) => ({ ...f, [campo]: valor }));

  function validar(): Erros {
    const e: Erros = {};
    if (form.nome.trim().length < 3) e.nome = "Informe o nome completo.";
    if (!cpfValido(form.cpf)) e.cpf = editando ? "Digite o CPF completo para confirmar a alteração." : "CPF inválido: confira os dígitos.";
    if (!telefoneValido(form.telefone)) e.telefone = "Informe o telefone com DDD.";
    if (form.bairro.trim().length < 2) e.bairro = "Informe o bairro.";
    if (!form.dataNascimento) e.dataNascimento = "Informe a data de nascimento.";
    else if (idadeEmAnos(form.dataNascimento) < 60) e.dataNascimento = "O Guardião é para pessoas com 60 anos ou mais.";
    const limite = Number(form.limitePix);
    if (form.limitePix === "" || Number.isNaN(limite) || limite < 0) e.limitePix = "Informe um limite de zero ou mais.";
    return e;
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) return;
    setEnviando(true);
    try {
      const dados = { ...form, limitePix: Number(form.limitePix) };
      if (editando) await api.idosos.atualizar(editando.id, dados);
      else await api.idosos.cadastrar(dados);
      setMensagem({ tipo: "sucesso", texto: editando ? "Idoso atualizado." : "Idoso cadastrado." });
      fechar();
      await recarregarPessoas();
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      else setErros({ geral: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }

  function novo() {
    setEditando(null);
    setForm(VAZIO);
    setErros({});
    setAberto(true);
  }

  function editar(i: Idoso) {
    setEditando(i);
    setErros({});
    setForm({ nome: i.nome, cpf: "", telefone: mascaraTelefone(i.telefone), bairro: i.bairro,
      dataNascimento: i.dataNascimento, limitePix: String(i.limitePix) });
    setAberto(true);
  }

  function fechar() {
    setAberto(false);
    setEditando(null);
  }

  async function excluir() {
    if (!excluindo) return;
    try {
      await api.idosos.remover(excluindo.id);
      setMensagem({ tipo: "sucesso", texto: "Idoso excluído." });
      await recarregarPessoas();
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    } finally {
      setExcluindo(null);
    }
  }

  const lista = idosos.filter((i) => i.nome.toLowerCase().includes(busca.toLowerCase()));

  return (
    <>
      {mensagem && <Mensagem tipo={mensagem.tipo}>{mensagem.texto}</Mensagem>}
      <BarraLista textoNovo="Novo idoso" aoNovo={novo}>
        <CampoBusca valor={busca} aoMudar={setBusca} rotulo="Buscar idoso…" />
      </BarraLista>
      <Tabela colunas={["Nome", "Nascimento", "Limite Pix", "Guardiões"]} vazio={lista.length === 0}>
        {lista.map((i) => (
          <tr key={i.id}>
            <Principal titulo={i.nome} detalhe={`${i.bairro} · ${mascaraTelefone(i.telefone)}`} />
            <Celula className="whitespace-nowrap">{formatarData(i.dataNascimento)} <span className="text-stone-500">({i.idade} anos)</span></Celula>
            <Celula className="whitespace-nowrap">{formatarMoeda(i.limitePix)}</Celula>
            <Celula>
              {i.qtGuardioes === 0
                ? <span className="text-sm font-semibold text-red-700">Sem guardião</span>
                : <span>{i.qtGuardioes}</span>}
            </Celula>
            <Acoes aoEditar={() => editar(i)} aoExcluir={() => setExcluindo(i)} />
          </tr>
        ))}
      </Tabela>

      <FormularioJanela titulo={editando ? `Editar ${editando.nome}` : "Novo idoso"} aberta={aberto} aoFechar={fechar}
        onSubmit={salvar} enviando={enviando}>
        {erros.geral && <Mensagem tipo="erro">{erros.geral}</Mensagem>}
        <Campo id="idoso-nome" rotulo="Nome completo" value={form.nome} onChange={(e) => alterar("nome", e.target.value)} erro={erros.nome} maxLength={80} autoFocus />
        <div className="grid gap-x-3 sm:grid-cols-2">
          <Campo id="idoso-cpf" rotulo="CPF" inputMode="numeric" value={form.cpf} onChange={(e) => alterar("cpf", mascaraCpf(e.target.value))}
            erro={erros.cpf} placeholder={editando ? editando.cpfMascarado : "000.000.000-00"} />
          <Campo id="idoso-telefone" rotulo="Telefone" inputMode="tel" value={form.telefone}
            onChange={(e) => alterar("telefone", mascaraTelefone(e.target.value))} erro={erros.telefone} placeholder="(11) 98765-4321" />
        </div>
        <Campo id="idoso-bairro" rotulo="Bairro" value={form.bairro} onChange={(e) => alterar("bairro", e.target.value)} erro={erros.bairro} maxLength={60} />
        <div className="grid gap-x-3 sm:grid-cols-2">
          <Campo id="idoso-nascimento" rotulo="Data de nascimento" type="date" value={form.dataNascimento}
            onChange={(e) => alterar("dataNascimento", e.target.value)} erro={erros.dataNascimento} />
          <Campo id="idoso-limite" rotulo="Limite de Pix (R$)" type="number" min={0} step="50" value={form.limitePix}
            onChange={(e) => alterar("limitePix", e.target.value)} erro={erros.limitePix} dica="Acima disso, um guardião precisa aprovar." />
        </div>
      </FormularioJanela>

      {excluindo && (
        <ConfirmarExclusao texto={`Excluir ${excluindo.nome}? Os vínculos com guardiões também serão removidos.`}
          aoConfirmar={excluir} aoCancelar={() => setExcluindo(null)} />
      )}
    </>
  );
}
