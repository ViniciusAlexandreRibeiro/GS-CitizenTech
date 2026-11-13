import { useEffect, useRef, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode } from "react";
import { Link } from "react-router";
import { useUsuario } from "../context/UsuarioContext";
import { Icone } from "./Icone";

/** Define o título da aba do navegador em cada página. */
export function useTitulo(titulo: string) {
  useEffect(() => {
    document.title = `${titulo} · Guardião`;
  }, [titulo]);
}

export function Pagina({ titulo, subtitulo, tituloAba, acoes, children }: {
  titulo: string;
  subtitulo?: string;
  tituloAba?: string;
  acoes?: ReactNode;
  children: ReactNode;
}) {
  useTitulo(tituloAba ?? titulo);
  return (
    <main className="mx-auto max-w-6xl px-4 py-6 sm:py-10">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight sm:text-3xl">{titulo}</h1>
          {subtitulo && <p className="mt-1 text-stone-600">{subtitulo}</p>}
        </div>
        {acoes}
      </div>
      <div className="mt-6 sm:mt-8">{children}</div>
    </main>
  );
}

type TipoMensagem = "erro" | "sucesso" | "aviso";

const estilos: Record<TipoMensagem, string> = {
  erro: "border-red-300 bg-red-50 text-red-900",
  sucesso: "border-green-300 bg-green-50 text-green-900",
  aviso: "border-amber-300 bg-amber-50 text-amber-900",
};

export function Mensagem({ tipo, children }: { tipo: TipoMensagem; children: ReactNode }) {
  const caixa = useRef<HTMLDivElement>(null);
  // Leva a mensagem para a área visível (no celular o formulário fica longe do topo)
  useEffect(() => {
    if (tipo !== "aviso") caixa.current?.scrollIntoView({ block: "nearest", behavior: "smooth" });
  }, [tipo, children]);
  return (
    <div ref={caixa} role={tipo === "erro" ? "alert" : "status"} className={`mb-4 flex gap-2 rounded-xl border p-3 ${estilos[tipo]}`}>
      <Icone nome={tipo === "sucesso" ? "check" : "alerta"} className="mt-0.5 shrink-0" />
      <div>{children}</div>
    </div>
  );
}

interface CampoProps extends InputHTMLAttributes<HTMLInputElement> {
  id: string;
  rotulo: string;
  erro?: string;
  dica?: string;
}

/** Campo de formulário com rótulo e mensagem de erro ligada por aria-describedby. */
export function Campo({ id, rotulo, erro, dica, className, ...props }: CampoProps) {
  return (
    <div className="mb-4">
      <label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-stone-700">
        {rotulo}
      </label>
      <input
        id={id}
        aria-invalid={erro ? true : undefined}
        aria-describedby={erro ? `${id}-erro` : dica ? `${id}-dica` : undefined}
        className={`min-h-12 w-full rounded-xl border bg-white px-3 ${erro ? "border-red-600 bg-red-50" : "border-stone-300 focus:border-marca-600"} ${className ?? ""}`}
        {...props}
      />
      {dica && !erro && <p id={`${id}-dica`} className="mt-1 text-sm text-stone-600">{dica}</p>}
      {erro && <p id={`${id}-erro`} className="mt-1 text-sm font-semibold text-red-700">{erro}</p>}
    </div>
  );
}

export function CampoSelect({ id, rotulo, erro, value, onChange, children }: {
  id: string;
  rotulo: string;
  erro?: string;
  value: string | number;
  onChange: (valor: string) => void;
  children: ReactNode;
}) {
  return (
    <div className="mb-4">
      <label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-stone-700">{rotulo}</label>
      <select
        id={id}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        aria-invalid={erro ? true : undefined}
        aria-describedby={erro ? `${id}-erro` : undefined}
        className={`min-h-12 w-full rounded-xl border bg-white px-3 ${erro ? "border-red-600 bg-red-50" : "border-stone-300"}`}
      >
        {children}
      </select>
      {erro && <p id={`${id}-erro`} className="mt-1 text-sm font-semibold text-red-700">{erro}</p>}
    </div>
  );
}

const variantes = {
  primario: "bg-marca-600 text-white shadow-sm hover:bg-marca-700",
  perigo: "bg-perigo text-white hover:bg-red-800",
  secundario: "border-2 border-stone-300 bg-white text-stone-900 hover:bg-stone-100",
  contorno: "border-2 border-white/80 bg-transparent text-white hover:bg-white/10",
};

export function Botao({ variante = "primario", className, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & {
  variante?: keyof typeof variantes;
}) {
  return (
    <button
      className={`inline-flex min-h-12 items-center justify-center gap-2 rounded-xl px-5 font-bold disabled:cursor-not-allowed disabled:opacity-60 ${variantes[variante]} ${className ?? ""}`}
      {...props}
    />
  );
}

export function Carregando({ texto = "Carregando…" }: { texto?: string }) {
  return (
    <p role="status" className="py-6 text-stone-600">
      {texto}
    </p>
  );
}

const coresStatus: Record<string, string> = {
  PENDENTE: "bg-amber-50 text-amber-800 ring-amber-200",
  AGENDADA: "bg-indigo-50 text-indigo-800 ring-indigo-200",
  LIBERADA: "bg-green-50 text-green-800 ring-green-200",
  CONFIRMADA: "bg-green-50 text-green-800 ring-green-200",
  BLOQUEADA: "bg-red-50 text-red-800 ring-red-200",
  CANCELADA: "bg-stone-100 text-stone-600 ring-stone-200",
};

const textosStatus: Record<string, string> = {
  PENDENTE: "Aguardando",
  AGENDADA: "Agendada",
  LIBERADA: "Liberado",
  CONFIRMADA: "Confirmada",
  BLOQUEADA: "Bloqueado",
  CANCELADA: "Cancelada",
};

/** Status sempre com texto + cor (nunca só a cor). */
export function Etiqueta({ status }: { status: string }) {
  return (
    <span className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset ${coresStatus[status] ?? "bg-stone-100 ring-stone-200"}`}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden="true" />
      {textosStatus[status] ?? status}
    </span>
  );
}

/** Janela (modal) usada pelos formulários de cadastro. Fecha com Esc ou clicando fora. */
export function Janela({ titulo, aberta, aoFechar, children }: {
  titulo: string;
  aberta: boolean;
  aoFechar: () => void;
  children: ReactNode;
}) {
  const fechar = useRef(aoFechar);
  fechar.current = aoFechar;

  useEffect(() => {
    if (!aberta) return;
    const teclado = (e: KeyboardEvent) => e.key === "Escape" && fechar.current();
    window.addEventListener("keydown", teclado);
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", teclado);
      document.body.style.overflow = "";
    };
  }, [aberta]);

  if (!aberta) return null;
  return (
    <div className="fixed inset-0 z-40 flex items-end justify-center bg-black/40 sm:items-center sm:p-4"
      onMouseDown={(e) => e.target === e.currentTarget && aoFechar()}>
      <div role="dialog" aria-modal="true" aria-labelledby="janela-titulo"
        className="max-h-[92vh] w-full overflow-y-auto rounded-t-3xl bg-white p-6 shadow-2xl sm:max-w-lg sm:rounded-3xl">
        <div className="mb-5 flex items-center justify-between gap-4">
          <h2 id="janela-titulo" className="text-xl font-bold">{titulo}</h2>
          <button type="button" onClick={aoFechar} aria-label="Fechar"
            className="flex h-11 w-11 items-center justify-center rounded-full text-stone-500 hover:bg-stone-100">
            <Icone nome="x" />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

/** Caixa de confirmação antes de excluir (em vez do confirm() do navegador). */
export function ConfirmarExclusao({ texto, aoConfirmar, aoCancelar, ocupado }: {
  texto: string;
  aoConfirmar: () => void;
  aoCancelar: () => void;
  ocupado?: boolean;
}) {
  return (
    <div role="alertdialog" aria-modal="true" aria-labelledby="confirmar-titulo"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 id="confirmar-titulo" className="text-xl font-bold">Confirmar exclusão</h2>
        <p className="mt-2 text-stone-700">{texto}</p>
        <div className="mt-6 flex flex-col gap-2 sm:flex-row sm:justify-end">
          <Botao variante="secundario" onClick={aoCancelar} disabled={ocupado}>Cancelar</Botao>
          <Botao variante="perigo" onClick={aoConfirmar} disabled={ocupado} autoFocus>
            {ocupado ? "Excluindo…" : "Excluir"}
          </Botao>
        </div>
      </div>
    </div>
  );
}

/** Mostrado quando a página precisa de um idoso ou guardião escolhido no topo. */
export function EscolhaPerfil({ tipo }: { tipo: "idoso" | "guardiao" }) {
  const { erroApi } = useUsuario();
  if (erroApi) {
    // o aviso de erro já aparece no topo da página
    return <p className="text-stone-600">Não foi possível carregar as pessoas cadastradas.</p>;
  }
  return (
    <div className="rounded-2xl border border-dashed border-stone-300 bg-white p-6 text-center">
      <p className="text-lg font-semibold">
        Escolha {tipo === "idoso" ? "um idoso" : "um guardião"} em <strong>“Estou usando como”</strong>, no topo da página.
      </p>
      <p className="mt-2 text-stone-600">
        Ainda não tem cadastro? <Link to="/cadastros" className="font-bold text-marca-700 underline">Vá para Cadastros</Link>.
      </p>
    </div>
  );
}
