import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api, mensagemDeErro } from "../services/api";
import type { Guardiao, Idoso } from "../types";

// No MVP não há login: a pessoa escolhe "Estou usando como" no topo da página.
export type Perfil =
  | { tipo: "idoso"; id: number; nome: string }
  | { tipo: "guardiao"; id: number; nome: string }
  | { tipo: "admin" };

interface UsuarioContextValor {
  perfil: Perfil | null;
  escolherPerfil: (perfil: Perfil | null) => void;
  idosos: Idoso[];
  guardioes: Guardiao[];
  erroApi: string | null;
  recarregarPessoas: () => Promise<void>;
}

const UsuarioContext = createContext<UsuarioContextValor | null>(null);
const CHAVE = "guardiao.perfil";

function lerPerfilSalvo(): Perfil | null {
  try {
    const salvo = localStorage.getItem(CHAVE);
    return salvo ? (JSON.parse(salvo) as Perfil) : null;
  } catch {
    return null;
  }
}

export function UsuarioProvider({ children }: { children: ReactNode }) {
  const [perfil, setPerfil] = useState<Perfil | null>(lerPerfilSalvo);
  const [idosos, setIdosos] = useState<Idoso[]>([]);
  const [guardioes, setGuardioes] = useState<Guardiao[]>([]);
  const [erroApi, setErroApi] = useState<string | null>(null);

  const recarregarPessoas = useCallback(async () => {
    try {
      const [listaIdosos, listaGuardioes] = await Promise.all([api.idosos.listar(), api.guardioes.listar()]);
      setIdosos(listaIdosos);
      setGuardioes(listaGuardioes);
      setErroApi(null);
    } catch (erro) {
      setErroApi(mensagemDeErro(erro));
    }
  }, []);

  useEffect(() => {
    recarregarPessoas();
  }, [recarregarPessoas]);

  function escolherPerfil(novo: Perfil | null) {
    setPerfil(novo);
    try {
      if (novo) localStorage.setItem(CHAVE, JSON.stringify(novo));
      else localStorage.removeItem(CHAVE);
    } catch {
      // sem localStorage (modo privado): o perfil vale só até recarregar a página
    }
  }

  return (
    <UsuarioContext.Provider value={{ perfil, escolherPerfil, idosos, guardioes, erroApi, recarregarPessoas }}>
      {children}
    </UsuarioContext.Provider>
  );
}

export function useUsuario() {
  const contexto = useContext(UsuarioContext);
  if (!contexto) throw new Error("useUsuario precisa estar dentro de <UsuarioProvider>");
  return contexto;
}
