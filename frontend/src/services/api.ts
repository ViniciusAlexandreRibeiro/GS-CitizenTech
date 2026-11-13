import type {
  CadastroEmpresa, CadastroGuardiao, CadastroIdoso, CadastroPrestador, CadastroSolicitacao, CadastroVinculo,
  CadastroVisita, ContatoRecorrente, Empresa, Guardiao, Idoso, IdosoSemGuardiao, Indicadores, Prestador,
  ResultadoConferencia, Solicitacao, SolicitacoesPorTipo, TempoResposta, TentativasBairro, ValorProtegido,
  Vinculo, Visita,
} from "../types";

// Endereço da API Java. Pode ser trocado criando o arquivo .env com VITE_API_URL=...
export const API_URL: string = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

/** Erro devolvido pela API (ou pela falta dela), com a mensagem pronta para mostrar na tela. */
export class ApiError extends Error {
  status: number;
  campo?: string;

  constructor(status: number, mensagem: string, campo?: string) {
    super(mensagem);
    this.status = status;
    this.campo = campo;
  }
}

async function requisicao<T>(caminho: string, metodo = "GET", corpo?: unknown): Promise<T> {
  let resposta: Response;
  try {
    resposta = await fetch(API_URL + caminho, {
      method: metodo,
      headers: corpo === undefined ? undefined : { "Content-Type": "application/json" },
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ApiError(0, "Servidor indisponível. Verifique se a API Java está rodando em " + API_URL + ".");
  }

  if (resposta.status === 204) {
    return undefined as T;
  }

  const texto = await resposta.text();
  const dados = texto ? JSON.parse(texto) : undefined;

  if (!resposta.ok) {
    throw new ApiError(resposta.status, dados?.mensagem ?? "Não foi possível concluir a operação.", dados?.campo);
  }
  return dados as T;
}

function filtros(parametros: Record<string, string | number | null | undefined>): string {
  const busca = new URLSearchParams();
  Object.entries(parametros).forEach(([chave, valor]) => {
    if (valor !== null && valor !== undefined && valor !== "") busca.set(chave, String(valor));
  });
  const texto = busca.toString();
  return texto ? "?" + texto : "";
}

export const api = {
  status: () => requisicao<{ api: string; banco: string; mensagem?: string }>("/status"),

  idosos: {
    listar: () => requisicao<Idoso[]>("/idosos"),
    buscar: (id: number) => requisicao<Idoso>(`/idosos/${id}`),
    cadastrar: (dados: CadastroIdoso) => requisicao<Idoso>("/idosos", "POST", dados),
    atualizar: (id: number, dados: CadastroIdoso) => requisicao<Idoso>(`/idosos/${id}`, "PUT", dados),
    remover: (id: number) => requisicao<void>(`/idosos/${id}`, "DELETE"),
  },

  guardioes: {
    listar: () => requisicao<Guardiao[]>("/guardioes"),
    cadastrar: (dados: CadastroGuardiao) => requisicao<Guardiao>("/guardioes", "POST", dados),
    atualizar: (id: number, dados: CadastroGuardiao) => requisicao<Guardiao>(`/guardioes/${id}`, "PUT", dados),
    remover: (id: number) => requisicao<void>(`/guardioes/${id}`, "DELETE"),
  },

  empresas: {
    listar: () => requisicao<Empresa[]>("/empresas"),
    cadastrar: (dados: CadastroEmpresa) => requisicao<Empresa>("/empresas", "POST", dados),
    atualizar: (id: number, dados: CadastroEmpresa) => requisicao<Empresa>(`/empresas/${id}`, "PUT", dados),
    remover: (id: number) => requisicao<void>(`/empresas/${id}`, "DELETE"),
  },

  prestadores: {
    listar: (idEmpresa?: number) => requisicao<Prestador[]>("/prestadores" + filtros({ empresa: idEmpresa })),
    cadastrar: (dados: CadastroPrestador) => requisicao<Prestador>("/prestadores", "POST", dados),
    atualizar: (id: number, dados: CadastroPrestador) => requisicao<Prestador>(`/prestadores/${id}`, "PUT", dados),
    remover: (id: number) => requisicao<void>(`/prestadores/${id}`, "DELETE"),
  },

  vinculos: {
    listar: (idIdoso?: number, idGuardiao?: number) =>
      requisicao<Vinculo[]>("/vinculos" + filtros({ idoso: idIdoso, guardiao: idGuardiao })),
    cadastrar: (dados: CadastroVinculo) => requisicao<Vinculo>("/vinculos", "POST", dados),
    atualizar: (idIdoso: number, idGuardiao: number, dados: CadastroVinculo) =>
      requisicao<Vinculo>(`/vinculos/${idIdoso}/${idGuardiao}`, "PUT", dados),
    remover: (idIdoso: number, idGuardiao: number) => requisicao<void>(`/vinculos/${idIdoso}/${idGuardiao}`, "DELETE"),
  },

  visitas: {
    listar: (idIdoso?: number, status?: string) => requisicao<Visita[]>("/visitas" + filtros({ idoso: idIdoso, status })),
    agendar: (dados: CadastroVisita) => requisicao<Visita>("/visitas", "POST", dados),
    atualizar: (id: number, dados: CadastroVisita) => requisicao<Visita>(`/visitas/${id}`, "PUT", dados),
    remover: (id: number) => requisicao<void>(`/visitas/${id}`, "DELETE"),
    conferir: (idIdoso: number, codigo: string) =>
      requisicao<ResultadoConferencia>("/visitas/conferir", "POST", { idIdoso, codigo }),
  },

  solicitacoes: {
    listar: (filtro: { idoso?: number; guardiao?: number; status?: string }) =>
      requisicao<Solicitacao[]>("/solicitacoes" + filtros(filtro)),
    buscar: (id: number) => requisicao<Solicitacao>(`/solicitacoes/${id}`),
    abrir: (dados: CadastroSolicitacao) => requisicao<Solicitacao>("/solicitacoes", "POST", dados),
    atualizar: (id: number, dados: CadastroSolicitacao) => requisicao<Solicitacao>(`/solicitacoes/${id}`, "PUT", dados),
    responder: (id: number, idGuardiao: number, decisao: "LIBERAR" | "BLOQUEAR", orientacao: string) =>
      requisicao<Solicitacao>(`/solicitacoes/${id}/resposta`, "PATCH", { idGuardiao, decisao, orientacao }),
    remover: (id: number) => requisicao<void>(`/solicitacoes/${id}`, "DELETE"),
    bloqueios: (contato: string) =>
      requisicao<{ contato: string; bloqueios: number }>("/solicitacoes/bloqueios" + filtros({ contato })),
  },

  relatorios: {
    indicadores: () => requisicao<Indicadores>("/relatorios/indicadores"),
    valorProtegido: () => requisicao<ValorProtegido[]>("/relatorios/valor-protegido"),
    portaPorBairro: () => requisicao<TentativasBairro[]>("/relatorios/porta-por-bairro"),
    porTipo: () => requisicao<SolicitacoesPorTipo[]>("/relatorios/por-tipo"),
    tempoResposta: () => requisicao<TempoResposta[]>("/relatorios/tempo-resposta"),
    contatosRecorrentes: () => requisicao<ContatoRecorrente[]>("/relatorios/contatos-recorrentes"),
    idososSemGuardiao: () => requisicao<IdosoSemGuardiao[]>("/relatorios/idosos-sem-guardiao"),
  },
};

/** Converte qualquer erro em texto para exibir. */
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ApiError) return erro.message;
  if (erro instanceof Error) return erro.message;
  return "Algo deu errado. Tente novamente.";
}
