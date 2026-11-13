import type { FormEvent, ReactNode } from "react";
import { Botao, Janela } from "../../components/Ui";

// Peças reaproveitadas pelas abas de cadastro e pela página de visitas.

/** Linha acima da tabela: busca/filtros à esquerda e o botão "Novo" à direita. */
export function BarraLista({ children, textoNovo, aoNovo }: { children?: ReactNode; textoNovo: string; aoNovo: () => void }) {
  return (
    <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center">
      <div className="flex flex-1 flex-col gap-3 sm:flex-row">{children}</div>
      <Botao onClick={aoNovo}>+ {textoNovo}</Botao>
    </div>
  );
}

export function CampoBusca({ valor, aoMudar, rotulo }: { valor: string; aoMudar: (v: string) => void; rotulo: string }) {
  return (
    <label className="sm:w-80">
      <span className="sr-only">{rotulo}</span>
      <input value={valor} onChange={(e) => aoMudar(e.target.value)} placeholder={rotulo}
        className="min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3" />
    </label>
  );
}

export function Filtro({ valor, aoMudar, rotulo, children }: {
  valor: string; aoMudar: (v: string) => void; rotulo: string; children: ReactNode;
}) {
  return (
    <label className="sm:w-60">
      <span className="sr-only">{rotulo}</span>
      <select value={valor} onChange={(e) => aoMudar(e.target.value)}
        className="min-h-11 w-full rounded-xl border border-stone-300 bg-white px-3">
        {children}
      </select>
    </label>
  );
}

export function Tabela({ colunas, children, vazio }: { colunas: string[]; children: ReactNode; vazio: boolean }) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-stone-200 bg-white">
      <table className="tabela-responsiva w-full text-left">
        <thead className="border-b border-stone-200 text-xs font-semibold uppercase tracking-wide text-stone-500">
          <tr>
            {colunas.map((c) => <th key={c} scope="col" className="px-4 py-3">{c}</th>)}
            <th scope="col" className="px-4 py-3"><span className="sr-only">Ações</span></th>
          </tr>
        </thead>
        <tbody className="divide-y divide-stone-100">
          {children}
          {vazio && <tr><td colSpan={colunas.length + 1} className="px-4 py-8 text-center text-stone-500">Nenhum registro encontrado.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}

/** Célula com nome em destaque e um detalhe menor embaixo. */
export function Principal({ titulo, detalhe }: { titulo: ReactNode; detalhe?: ReactNode }) {
  return (
    <td className="px-4 py-3">
      <span className="block font-semibold">{titulo}</span>
      {detalhe && <span className="block text-sm text-stone-500">{detalhe}</span>}
    </td>
  );
}

export function Celula({ children, className }: { children: ReactNode; className?: string }) {
  return <td className={`px-4 py-3 ${className ?? ""}`}>{children}</td>;
}

export function Acoes({ aoEditar, aoExcluir, textoEditar = "Editar", extra }: {
  aoEditar?: () => void;
  aoExcluir: () => void;
  textoEditar?: string;
  extra?: ReactNode;
}) {
  return (
    <td className="px-4 py-3">
      <div className="flex justify-end gap-4 whitespace-nowrap text-sm font-semibold">
        {extra}
        {aoEditar && <button className="text-marca-700 hover:underline" onClick={aoEditar}>{textoEditar}</button>}
        <button className="text-stone-500 hover:text-red-700 hover:underline" onClick={aoExcluir}>Excluir</button>
      </div>
    </td>
  );
}

/** Formulário dentro de uma janela, com os botões Cancelar e Salvar no rodapé. */
export function FormularioJanela({ titulo, aberta, aoFechar, onSubmit, enviando, textoSalvar = "Salvar", children }: {
  titulo: string;
  aberta: boolean;
  aoFechar: () => void;
  onSubmit: (e: FormEvent) => void;
  enviando: boolean;
  textoSalvar?: string;
  children: ReactNode;
}) {
  return (
    <Janela titulo={titulo} aberta={aberta} aoFechar={aoFechar}>
      <form onSubmit={onSubmit} noValidate>
        {children}
        <div className="mt-2 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <Botao type="button" variante="secundario" onClick={aoFechar}>Cancelar</Botao>
          <Botao type="submit" disabled={enviando}>{enviando ? "Salvando…" : textoSalvar}</Botao>
        </div>
      </form>
    </Janela>
  );
}
