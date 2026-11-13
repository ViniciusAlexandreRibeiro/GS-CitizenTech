import { useState } from "react";
import { NavLink } from "react-router";
import { useUsuario, type Perfil } from "../context/UsuarioContext";
import { Icone } from "./Icone";

const LINKS = [
  { para: "/", texto: "Início" },
  { para: "/porta", texto: "Porta" },
  { para: "/pedido", texto: "Pedir ajuda" },
  { para: "/guardiao", texto: "Painel" },
  { para: "/visitas", texto: "Visitas" },
  { para: "/cadastros", texto: "Cadastros" },
  { para: "/impacto", texto: "Impacto" },
];

function valorDoPerfil(perfil: Perfil | null): string {
  if (!perfil) return "";
  return perfil.tipo === "admin" ? "admin" : `${perfil.tipo}:${perfil.id}`;
}

/** Seletor "Estou usando como" (no MVP ele substitui o login). */
function SeletorPerfil({ escuro }: { escuro?: boolean }) {
  const { perfil, escolherPerfil, idosos, guardioes } = useUsuario();

  function trocar(valor: string) {
    if (valor === "") return escolherPerfil(null);
    if (valor === "admin") return escolherPerfil({ tipo: "admin" });
    const [tipo, id] = valor.split(":");
    const lista = tipo === "idoso" ? idosos : guardioes;
    const pessoa = lista.find((p) => p.id === Number(id));
    if (pessoa) escolherPerfil({ tipo: tipo as "idoso" | "guardiao", id: pessoa.id, nome: pessoa.nome });
  }

  return (
    <label className={`flex min-h-11 items-center gap-2 rounded-full px-3 ${escuro ? "bg-marca-900/60 text-white" : "bg-white text-stone-900 ring-1 ring-stone-300"}`}>
      <span className="sr-only">Estou usando como</span>
      <span aria-hidden="true" className="text-sm opacity-80">Usando como</span>
      <select
        value={valorDoPerfil(perfil)}
        onChange={(e) => trocar(e.target.value)}
        className={`max-w-48 cursor-pointer appearance-none bg-transparent py-2 pr-1 font-semibold outline-none ${escuro ? "text-white" : ""}`}
      >
        <option value="" className="text-stone-900">Escolha…</option>
        <optgroup label="Idosos" className="text-stone-900">
          {idosos.map((i) => <option key={i.id} value={`idoso:${i.id}`}>{i.nome}</option>)}
        </optgroup>
        <optgroup label="Guardiões" className="text-stone-900">
          {guardioes.map((g) => <option key={g.id} value={`guardiao:${g.id}`}>{g.nome}</option>)}
        </optgroup>
        <option value="admin" className="text-stone-900">Empresa / administração</option>
      </select>
      <span aria-hidden="true" className="text-xs opacity-70">▾</span>
    </label>
  );
}

export function Header() {
  const [menuAberto, setMenuAberto] = useState(false);

  return (
    <header className="sticky top-0 z-30 bg-marca-800 text-white shadow-sm">
      <div className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-4">
        <NavLink to="/" className="flex shrink-0 items-center gap-2 text-xl font-extrabold" onClick={() => setMenuAberto(false)}>
          <Icone nome="escudo" tamanho={26} className="text-green-300" />
          Guardião
        </NavLink>

        <nav aria-label="Menu principal" className="hidden lg:block">
          <ul className="flex gap-1">
            {LINKS.map((link) => (
              <li key={link.para}>
                <NavLink
                  to={link.para}
                  end={link.para === "/"}
                  className={({ isActive }) =>
                    `block rounded-full px-3 py-1.5 text-[0.85rem] font-medium transition-colors ${isActive ? "bg-white text-marca-800" : "text-green-50 hover:bg-white/10"}`
                  }
                >
                  {link.texto}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <div className="ml-auto hidden lg:block">
          <SeletorPerfil escuro />
        </div>

        <button
          type="button"
          className="ml-auto flex h-12 w-12 items-center justify-center rounded-full hover:bg-white/10 lg:hidden"
          aria-expanded={menuAberto}
          aria-controls="menu-celular"
          aria-label={menuAberto ? "Fechar menu" : "Abrir menu"}
          onClick={() => setMenuAberto(!menuAberto)}
        >
          <Icone nome={menuAberto ? "x" : "menu"} tamanho={26} />
        </button>
      </div>

      {menuAberto && (
        <nav id="menu-celular" aria-label="Menu principal" className="border-t border-white/10 px-4 pb-5 lg:hidden">
          <ul className="py-2">
            {LINKS.map((link) => (
              <li key={link.para}>
                <NavLink
                  to={link.para}
                  end={link.para === "/"}
                  onClick={() => setMenuAberto(false)}
                  className={({ isActive }) => `block rounded-xl px-3 py-3 text-lg ${isActive ? "bg-white/10 font-bold" : ""}`}
                >
                  {link.texto}
                </NavLink>
              </li>
            ))}
          </ul>
          <SeletorPerfil escuro />
        </nav>
      )}
    </header>
  );
}
