import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router";
import { Icone } from "../components/Icone";
import { Botao, EscolhaPerfil, Mensagem, Pagina } from "../components/Ui";
import { useUsuario } from "../context/UsuarioContext";
import { api, mensagemDeErro } from "../services/api";
import type { ResultadoConferencia, Vinculo } from "../types";
import { formatarDataHora, mascaraTelefone } from "../utils/formatos";
import { somenteDigitos } from "../utils/validacoes";

export function Porta() {
  const { perfil } = useUsuario();
  const [codigo, setCodigo] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState<ResultadoConferencia | null>(null);
  const [guardioes, setGuardioes] = useState<Vinculo[]>([]);

  const idIdoso = perfil?.tipo === "idoso" ? perfil.id : null;

  useEffect(() => {
    if (idIdoso !== null) api.vinculos.listar(idIdoso).then(setGuardioes).catch(() => setGuardioes([]));
  }, [idIdoso]);

  if (idIdoso === null) {
    return (
      <Pagina titulo="Alguém na porta?">
        <EscolhaPerfil tipo="idoso" />
      </Pagina>
    );
  }

  async function conferir(codigoDigitado: string) {
    setErro("");
    if (codigoDigitado !== "" && codigoDigitado.length !== 6) {
      setErro("O código tem 6 números. Confira com a pessoa.");
      return;
    }
    setEnviando(true);
    try {
      setResultado(await api.visitas.conferir(idIdoso!, codigoDigitado));
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setEnviando(false);
    }
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    conferir(codigo);
  }

  function recomecar() {
    setResultado(null);
    setCodigo("");
  }

  if (resultado?.resultado === "CONFIRMADA") {
    return (
      <main className="flex min-h-[80vh] flex-col items-center bg-marca-600 px-4 py-10 text-center text-white">
        <div className="rounded-full bg-white/20 p-6"><Icone nome="check" tamanho={72} /></div>
        <h1 className="mt-6 text-4xl font-black">PODE ABRIR</h1>
        <p className="mt-1 text-lg">Visita confirmada pelo Guardião</p>
        <div className="mt-8 w-full max-w-md rounded-2xl bg-white p-5 text-left text-stone-900">
          <p className="text-2xl font-extrabold">{resultado.visitante}</p>
          {resultado.funcao && <p className="text-stone-600">{resultado.funcao}</p>}
          <dl className="mt-3 space-y-1">
            {resultado.empresa && <div><dt className="inline font-bold">Empresa: </dt><dd className="inline">{resultado.empresa}</dd></div>}
            <div><dt className="inline font-bold">Horário: </dt><dd className="inline">{formatarDataHora(resultado.horario)}</dd></div>
          </dl>
        </div>
        <p className="mt-6">Mesmo assim, peça para ver o crachá.</p>
        <Botao variante="secundario" className="mt-8 w-full max-w-md" onClick={recomecar}>Voltar</Botao>
      </main>
    );
  }

  if (resultado?.resultado === "NEGADA") {
    const primeiro = guardioes[0];
    return (
      <main className="flex min-h-[80vh] flex-col items-center bg-perigo px-4 py-10 text-center text-white">
        <div className="rounded-full bg-white/20 p-6"><Icone nome="x" tamanho={72} /></div>
        <h1 className="mt-6 text-4xl font-black leading-tight">NÃO ABRA<br />A PORTA</h1>
        <p className="mt-3 max-w-md text-lg">{resultado.mensagem.replace("Não abra a porta. ", "")}</p>
        {resultado.guardioesAvisados.length > 0 && (
          <div className="mt-6 w-full max-w-md rounded-2xl bg-black/20 p-4 text-left">
            <p className="font-bold">Avisamos seus guardiões</p>
            <p>{resultado.guardioesAvisados.join(", ")} já podem ver o alerta.</p>
          </div>
        )}
        {primeiro && (
          <a href={`tel:${primeiro.telefoneGuardiao}`}
            className="mt-8 flex min-h-12 w-full max-w-md items-center justify-center gap-2 rounded-xl bg-white font-bold text-perigo">
            <Icone nome="telefone" /> Ligar para {primeiro.nomeGuardiao.split(" ")[0]} · {mascaraTelefone(primeiro.telefoneGuardiao)}
          </a>
        )}
        <Botao variante="contorno" className="mt-3 w-full max-w-md" onClick={recomecar}>
          Voltar
        </Botao>
      </main>
    );
  }

  return (
    <Pagina titulo="Alguém na porta?">
      <form onSubmit={enviar} noValidate className="mx-auto max-w-md rounded-2xl border border-stone-200 bg-white p-6">
        <p className="text-xl font-bold">
          Peça à pessoa o <span className="text-marca-700">código de 6 números</span> da visita.
        </p>
        <p className="mt-2 text-stone-600">Empresas sérias e a sua família agendam a visita antes e recebem esse código.</p>

        <label htmlFor="codigo" className="mt-6 block font-semibold">Código da visita</label>
        <input
          id="codigo"
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={6}
          value={codigo}
          onChange={(e) => setCodigo(somenteDigitos(e.target.value).slice(0, 6))}
          aria-describedby={erro ? "codigo-erro" : undefined}
          aria-invalid={erro ? true : undefined}
          className="mt-1 w-full rounded-xl border-2 border-marca-600 bg-white py-3 text-center font-mono text-4xl tracking-[0.4em]"
          placeholder="______"
        />
        {erro && <div id="codigo-erro" className="mt-3"><Mensagem tipo="erro">{erro}</Mensagem></div>}

        <Botao type="submit" className="mt-6 w-full text-lg" disabled={enviando}>
          {enviando ? "Conferindo…" : "Conferir código"}
        </Botao>
        <Botao type="button" variante="secundario" className="mt-3 w-full" disabled={enviando} onClick={() => conferir("")}>
          A pessoa diz que não tem código
        </Botao>
      </form>
      <p className="mt-6 text-center">
        <Link to="/" className="font-bold text-marca-700 underline">Voltar ao início</Link>
      </p>
    </Pagina>
  );
}
