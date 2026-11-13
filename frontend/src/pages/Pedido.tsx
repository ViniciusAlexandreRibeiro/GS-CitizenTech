import { useEffect, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import { Botao, Campo, EscolhaPerfil, Mensagem, Pagina } from "../components/Ui";
import { useUsuario } from "../context/UsuarioContext";
import { api, ApiError, mensagemDeErro } from "../services/api";
import type { Solicitacao, TipoSolicitacao } from "../types";
import { formatarMoeda, NOMES_TIPO } from "../utils/formatos";
import { temErros, type Erros } from "../utils/validacoes";

const TIPOS: TipoSolicitacao[] = ["PIX", "LIGACAO", "LINK", "MENSAGEM"];

const DICA_CONTATO: Record<string, string> = {
  PIX: "Número de quem pediu ou a chave Pix",
  LIGACAO: "Número que ligou",
  LINK: "Endereço do link (ex.: banco-atualiza.com)",
  MENSAGEM: "Número ou e-mail de quem mandou",
};

export function Pedido() {
  const { perfil, idosos } = useUsuario();
  const [parametros] = useSearchParams();
  const idEditar = parametros.get("editar") ? Number(parametros.get("editar")) : null;

  const tipoInicial = parametros.get("tipo") as TipoSolicitacao | null;
  const [tipo, setTipo] = useState<TipoSolicitacao>(tipoInicial && TIPOS.includes(tipoInicial) ? tipoInicial : "PIX");
  const [valor, setValor] = useState("");
  const [descricao, setDescricao] = useState("");
  const [contato, setContato] = useState("");
  const [bloqueios, setBloqueios] = useState(0);
  const [erros, setErros] = useState<Erros>({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState<Solicitacao | null>(null);

  const idIdoso = perfil?.tipo === "idoso" ? perfil.id : null;
  const idoso = idosos.find((i) => i.id === idIdoso);
  const valorNumero = Number(valor.replace(/\./g, "").replace(",", "."));

  // Modo edição: carrega o pedido que ainda está pendente
  useEffect(() => {
    if (idEditar === null) return;
    api.solicitacoes
      .buscar(idEditar)
      .then((s) => {
        setTipo(s.tipo);
        setDescricao(s.descricao);
        setValor(s.valor ? String(s.valor).replace(".", ",") : "");
        setContato(s.contatoSuspeito ?? "");
      })
      .catch((e) => setErroGeral(mensagemDeErro(e)));
  }, [idEditar]);

  if (idIdoso === null) {
    return (
      <Pagina titulo="Pedir ajuda">
        <EscolhaPerfil tipo="idoso" />
      </Pagina>
    );
  }

  async function verificarContato() {
    if (contato.trim().length < 5) return setBloqueios(0);
    try {
      const resposta = await api.solicitacoes.bloqueios(contato);
      setBloqueios(resposta.bloqueios);
    } catch {
      setBloqueios(0);
    }
  }

  function validar(): Erros {
    const novos: Erros = {};
    if (tipo === "PIX" && (!valor || !(valorNumero > 0))) novos.valor = "Informe quanto pediram (maior que zero).";
    if (tipo !== "PIX" && valor && !(valorNumero > 0)) novos.valor = "O valor precisa ser maior que zero.";
    if (descricao.trim().length < 5) novos.descricao = "Conte em poucas palavras o que aconteceu.";
    if (descricao.length > 400) novos.descricao = "Use no máximo 400 caracteres.";
    if (contato.length > 150) novos.contatoSuspeito = "Use no máximo 150 caracteres.";
    return novos;
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setErroGeral("");
    const encontrados = validar();
    setErros(encontrados);
    if (temErros(encontrados)) {
      setErroGeral("Confira os campos destacados.");
      return;
    }
    const dados = {
      idIdoso,
      tipo,
      descricao: descricao.trim(),
      valor: valor ? valorNumero : null,
      contatoSuspeito: contato.trim(),
    };
    setEnviando(true);
    try {
      const salvo = idEditar === null ? await api.solicitacoes.abrir(dados) : await api.solicitacoes.atualizar(idEditar, dados);
      setEnviado(salvo);
    } catch (e) {
      if (e instanceof ApiError && e.campo) setErros({ [e.campo]: e.message });
      setErroGeral(mensagemDeErro(e));
    } finally {
      setEnviando(false);
    }
  }

  if (enviado) {
    return (
      <Pagina titulo={idEditar === null ? "Pedido enviado" : "Pedido corrigido"}>
        <div className="mx-auto max-w-lg rounded-2xl border border-marca-600/40 bg-marca-50 p-6">
          <p className="text-xl font-bold">Avisamos seus guardiões.</p>
          <p className="mt-2 text-lg">
            <strong>Não pague, não clique e não passe nenhum dado</strong> até alguém responder.
          </p>
          {enviado.aprovacaoObrigatoria && (
            <p className="mt-3 rounded-lg bg-amber-50 p-3 text-amber-900">
              O valor passa do seu limite de {formatarMoeda(enviado.limitePix)}: a resposta de um guardião é obrigatória.
            </p>
          )}
          <Link to="/" className="mt-6 inline-flex min-h-12 items-center rounded-xl bg-marca-600 px-5 font-bold text-white">
            Ver respostas no início
          </Link>
        </div>
      </Pagina>
    );
  }

  return (
    <Pagina titulo={idEditar === null ? "Pedir ajuda" : "Corrigir pedido"} subtitulo="Conte o que aconteceu. Seus guardiões vão responder.">
      <form onSubmit={enviar} noValidate className="mx-auto max-w-xl rounded-2xl border border-stone-200 bg-white p-6">
        {erroGeral && <Mensagem tipo="erro">{erroGeral}</Mensagem>}

        <fieldset className="mb-4">
          <legend className="mb-2 text-sm font-semibold text-stone-700">Como pediram?</legend>
          <div className="flex flex-wrap gap-2">
            {TIPOS.map((t) => (
              <label key={t}
                className={`flex min-h-12 cursor-pointer items-center rounded-full border px-4 font-semibold ${tipo === t ? "border-marca-600 bg-marca-50 text-marca-800" : "border-stone-300"}`}>
                <input type="radio" name="tipo" value={t} checked={tipo === t} onChange={() => setTipo(t)} className="sr-only" />
                {NOMES_TIPO[t]}
              </label>
            ))}
          </div>
        </fieldset>

        {tipo === "PIX" && (
          <Campo id="valor" rotulo="Quanto pediram? (R$)" inputMode="decimal" placeholder="2.000,00"
            value={valor} onChange={(e) => setValor(e.target.value.replace(/[^\d.,]/g, ""))} erro={erros.valor}
            dica={idoso ? `Seu limite sem aprovação é ${formatarMoeda(idoso.limitePix)}.` : undefined} />
        )}

        <div className="mb-4">
          <label htmlFor="descricao" className="mb-1.5 block text-sm font-semibold text-stone-700">O que aconteceu?</label>
          <textarea id="descricao" rows={3} value={descricao} onChange={(e) => setDescricao(e.target.value)} maxLength={400}
            placeholder="Ex.: Dizem ser meu neto com número novo e pediram para pagar um boleto."
            aria-invalid={erros.descricao ? true : undefined} aria-describedby={erros.descricao ? "descricao-erro" : undefined}
            className={`w-full rounded-xl border bg-white p-3 ${erros.descricao ? "border-red-600 bg-red-50" : "border-stone-300"}`} />
          {erros.descricao && <p id="descricao-erro" className="mt-1 text-sm font-semibold text-red-700">{erros.descricao}</p>}
        </div>

        <Campo id="contato" rotulo="Número, link ou chave Pix (se tiver)" value={contato}
          onChange={(e) => setContato(e.target.value)} onBlur={verificarContato}
          dica={DICA_CONTATO[tipo]} erro={erros.contatoSuspeito} />

        {bloqueios > 0 && (
          <Mensagem tipo="aviso">
            <strong>Atenção:</strong> este contato já foi bloqueado <strong>{bloqueios} {bloqueios === 1 ? "vez" : "vezes"}</strong> por outros guardiões.
          </Mensagem>
        )}

        <Botao type="submit" className="w-full text-lg" disabled={enviando}>
          {enviando ? "Enviando…" : idEditar === null ? "Avisar meus guardiões" : "Salvar correção"}
        </Botao>
      </form>
    </Pagina>
  );
}
