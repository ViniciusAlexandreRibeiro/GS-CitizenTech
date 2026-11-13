import { useSearchParams } from "react-router";
import { Pagina } from "../components/Ui";
import { CadastroEmpresas } from "./cadastros/Empresas";
import { CadastroGuardioes } from "./cadastros/Guardioes";
import { CadastroIdosos } from "./cadastros/Idosos";
import { CadastroPrestadores } from "./cadastros/Prestadores";
import { CadastroVinculos } from "./cadastros/Vinculos";

const ABAS = [
  { id: "idosos", nome: "Idosos", componente: CadastroIdosos },
  { id: "guardioes", nome: "Guardiões", componente: CadastroGuardioes },
  { id: "vinculos", nome: "Vínculos", componente: CadastroVinculos },
  { id: "empresas", nome: "Empresas", componente: CadastroEmpresas },
  { id: "prestadores", nome: "Prestadores", componente: CadastroPrestadores },
];

export function Cadastros() {
  // a aba fica no endereço (#/cadastros?aba=guardioes), então dá para voltar e recarregar sem perder
  const [parametros, setParametros] = useSearchParams();
  const atual = ABAS.find((a) => a.id === parametros.get("aba")) ?? ABAS[0];
  const Conteudo = atual.componente;

  return (
    <Pagina titulo="Cadastros" subtitulo="Pessoas e empresas da rede de proteção.">
      <div role="tablist" aria-label="Tipo de cadastro" className="mb-6 flex gap-1 overflow-x-auto border-b border-stone-200">
        {ABAS.map((aba) => (
          <button
            key={aba.id}
            role="tab"
            id={`aba-${aba.id}`}
            aria-selected={aba.id === atual.id}
            aria-controls="painel-cadastro"
            onClick={() => setParametros({ aba: aba.id })}
            className={`-mb-px min-h-12 whitespace-nowrap border-b-2 px-4 font-semibold ${aba.id === atual.id ? "border-marca-600 text-stone-900" : "border-transparent text-stone-500 hover:text-stone-900"}`}
          >
            {aba.nome}
          </button>
        ))}
      </div>
      <section id="painel-cadastro" role="tabpanel" aria-labelledby={`aba-${atual.id}`}>
        <Conteudo />
      </section>
    </Pagina>
  );
}
