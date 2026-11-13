import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Campo, CampoSelect, ConfirmarExclusao, Mensagem } from "../../components/Ui";
import { api, ApiError, mensagemDeErro } from "../../services/api";
import type { Empresa, Prestador } from "../../types";
import { mascaraCpf } from "../../utils/formatos";
import { cpfValido, temErros, type Erros } from "../../utils/validacoes";
import { Acoes, BarraLista, Celula, Filtro, FormularioJanela, Principal, Tabela } from "./Comum";

const VAZIO = { idEmpresa: "", nome: "", cpf: "", funcao: "" };

export function CadastroPrestadores() {
  const [prestadores, setPrestadores] = useState<Prestador[]>([]);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [filtroEmpresa, setFiltroEmpresa] = useState("");
  const [form, setForm] = useState(VAZIO);
  const [aberto, setAberto] = useState(false);
  const [editando, setEditando] = useState<Prestador | null>(null);
  const [erros, setErros] = useState<Erros>({});
  const [mensagem, setMensagem] = useState<{ tipo: "erro" | "sucesso"; texto: string } | null>(null);
  const [excluindo, setExcluindo] = useState<Prestador | null>(null);
  const [enviando, setEnviando] = useState(false);

  const carregar = useCallback(async () => {
    try {
      setPrestadores(await api.prestadores.listar(filtroEmpresa ? Number(filtroEmpresa) : undefined));
    } catch (e) {
      setMensagem({ tipo: "erro", texto: mensagemDeErro(e) });
    }
  }, [filtroEmpresa]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  useEffect(() => {
    api.empresas.listar().then(setEmpresas).catch(() => setEmpresas([]));
  }, []);

  function validar(): Erros {
    const e: Erros = {};
    if (!form.idEmpresa) e.idEmpresa = "Selecione a empresa.";
    if (form.nome.trim().length < 3) e.nome = "Informe o nome completo.";
    if (!cpfValido(form.cpf)) e.cpf = editando ? "Digite o CPF completo para confirmar a alteração." : "CPF inválido: confira os dígitos.";
    if (form.funcao.trim().length < 3) e.funcao = "Informe a função (ex.: Técnico de medição).";
    return e;
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) return;
    setEnviando(true);
    try {
      const dados = { ...form, idEmpresa: Number(form.idEmpresa) };
      if (editando) await api.prestadores.atualizar(editando.id, dados);
      else await api.prestadores.cadastrar(dados);
      setMensagem({ tipo: "sucesso", texto: editando ? "Prestador atualizado." : "Prestador cadastrado." });
      setAberto(false);
      await carregar();
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      else setErros({ geral: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }

  function abrir(p: Prestador | null) {
    setEditando(p);
    setErros({});
    setForm(p ? { idEmpresa: String(p.idEmpresa), nome: p.nome, cpf: "", funcao: p.funcao } : VAZIO);
    setAberto(true);
  }

  async function excluir() {
    if (!excluindo) return;
    try {
      await api.prestadores.remover(excluindo.id);
      setMensagem({ tipo: "sucesso", texto: "Prestador excluído." });
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
      <BarraLista textoNovo="Novo prestador" aoNovo={() => abrir(null)}>
        <Filtro valor={filtroEmpresa} aoMudar={setFiltroEmpresa} rotulo="Filtrar por empresa">
          <option value="">Todas as empresas</option>
          {empresas.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
        </Filtro>
      </BarraLista>
      <Tabela colunas={["Nome", "Empresa"]} vazio={prestadores.length === 0}>
        {prestadores.map((p) => (
          <tr key={p.id}>
            <Principal titulo={p.nome} detalhe={p.funcao} />
            <Celula>{p.nomeEmpresa}</Celula>
            <Acoes aoEditar={() => abrir(p)} aoExcluir={() => setExcluindo(p)} />
          </tr>
        ))}
      </Tabela>

      <FormularioJanela titulo={editando ? `Editar ${editando.nome}` : "Novo prestador"} aberta={aberto}
        aoFechar={() => setAberto(false)} onSubmit={salvar} enviando={enviando}>
        {erros.geral && <Mensagem tipo="erro">{erros.geral}</Mensagem>}
        <CampoSelect id="prestador-empresa" rotulo="Empresa" value={form.idEmpresa} erro={erros.idEmpresa}
          onChange={(v) => setForm({ ...form, idEmpresa: v })}>
          <option value="">Selecione…</option>
          {empresas.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
        </CampoSelect>
        <Campo id="prestador-nome" rotulo="Nome completo" value={form.nome} maxLength={80}
          onChange={(e) => setForm({ ...form, nome: e.target.value })} erro={erros.nome} />
        <div className="grid gap-x-3 sm:grid-cols-2">
          <Campo id="prestador-cpf" rotulo="CPF" inputMode="numeric" value={form.cpf}
            placeholder={editando ? editando.cpfMascarado : "000.000.000-00"}
            onChange={(e) => setForm({ ...form, cpf: mascaraCpf(e.target.value) })} erro={erros.cpf} />
          <Campo id="prestador-funcao" rotulo="Função" value={form.funcao} maxLength={50}
            onChange={(e) => setForm({ ...form, funcao: e.target.value })} erro={erros.funcao} />
        </div>
      </FormularioJanela>

      {excluindo && (
        <ConfirmarExclusao texto={`Excluir ${excluindo.nome}? Prestadores com visitas registradas não podem ser excluídos.`}
          aoConfirmar={excluir} aoCancelar={() => setExcluindo(null)} />
      )}
    </>
  );
}
