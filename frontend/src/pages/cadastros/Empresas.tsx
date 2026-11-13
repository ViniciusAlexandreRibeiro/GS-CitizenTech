import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Campo, CampoSelect, ConfirmarExclusao, Mensagem } from "../../components/Ui";
import { api, ApiError, mensagemDeErro } from "../../services/api";
import type { Empresa, Setor } from "../../types";
import { mascaraCnpj, NOMES_SETOR } from "../../utils/formatos";
import { cnpjValido, temErros, type Erros } from "../../utils/validacoes";
import { Acoes, BarraLista, Celula, FormularioJanela, Principal, Tabela } from "./Comum";

const VAZIO = { nome: "", cnpj: "", setor: "" as Setor | "" };

export function CadastroEmpresas() {
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [form, setForm] = useState(VAZIO);
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState<Empresa | null>(null);
  const [erros, setErros] = useState<Erros>({});
  const [mensagem, setMensagem] = useState<{ tipo: "erro" | "sucesso"; texto: string } | null>(null);
  const [excluindo, setExcluindo] = useState<Empresa | null>(null);
  const [enviando, setEnviando] = useState(false);

  const carregar = useCallback(async () => {
    try {
      setEmpresas(await api.empresas.listar());
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function validar(): Erros {
    const e: Erros = {};
    if (form.nome.trim().length < 2) e.nome = "Informe o nome da empresa.";
    if (!cnpjValido(form.cnpj)) e.cnpj = "CNPJ inválido: confira os dígitos.";
    if (!form.setor) e.setor = "Selecione o setor.";
    return e;
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) return;
    setEnviando(true);
    try {
      if (editando) await api.empresas.atualizar(editando.id, form);
      else await api.empresas.cadastrar(form);
      setMensagem({ tipo: "sucesso", texto: editando ? "Empresa atualizada." : "Empresa cadastrada." });
      setAberto(false);
      await carregar();
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      else setErros({ geral: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }

  function abrir(e: Empresa | null) {
    setEditando(e);
    setErros({});
    setForm(e ? { nome: e.nome, cnpj: e.cnpj, setor: e.setor } : VAZIO);
    setAberto(true);
  }

  async function excluir() {
    if (!excluindo) return;
    try {
      await api.empresas.remover(excluindo.id);
      setMensagem({ tipo: "sucesso", texto: "Empresa excluída." });
      await carregar();
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    } finally {
      setExcluindo(null);
    }
  }

  return (
    <>
      {mensagem && <Mensagem tipo={mensagem.tipo}>{mensagem.texto}</Mensagem>}
      <BarraLista textoNovo="Nova empresa" aoNovo={() => abrir(null)} />
      <Tabela colunas={["Empresa", "Setor"]} vazio={empresas.length === 0}>
        {empresas.map((e) => (
          <tr key={e.id}>
            <Principal titulo={e.nome} detalhe={`CNPJ ${e.cnpj}`} />
            <Celula>{NOMES_SETOR[e.setor]}</Celula>
            <Acoes aoEditar={() => abrir(e)} aoExcluir={() => setExcluindo(e)} />
          </tr>
        ))}
      </Tabela>

      <FormularioJanela titulo={editando ? `Editar ${editando.nome}` : "Nova empresa"} aberta={aberto}
        aoFechar={() => setAberto(false)} onSubmit={salvar} enviando={enviando}>
        {erros.geral && <Mensagem tipo="erro">{erros.geral}</Mensagem>}
        <Campo id="empresa-nome" rotulo="Nome" value={form.nome} maxLength={80} autoFocus
          onChange={(e) => setForm({ ...form, nome: e.target.value })} erro={erros.nome} />
        <div className="grid gap-x-3 sm:grid-cols-2">
          <Campo id="empresa-cnpj" rotulo="CNPJ" inputMode="numeric" value={form.cnpj} placeholder="00.000.000/0000-00"
            onChange={(e) => setForm({ ...form, cnpj: mascaraCnpj(e.target.value) })} erro={erros.cnpj} />
          <CampoSelect id="empresa-setor" rotulo="Setor" value={form.setor} erro={erros.setor}
            onChange={(v) => setForm({ ...form, setor: v as Setor })}>
            <option value="">Selecione…</option>
            {Object.entries(NOMES_SETOR).map(([valor, nome]) => <option key={valor} value={valor}>{nome}</option>)}
          </CampoSelect>
        </div>
      </FormularioJanela>

      {excluindo && (
        <ConfirmarExclusao texto={`Excluir ${excluindo.nome}? Empresas com prestadores cadastrados não podem ser excluídas.`}
          aoConfirmar={excluir} aoCancelar={() => setExcluindo(null)} />
      )}
    </>
  );
}
