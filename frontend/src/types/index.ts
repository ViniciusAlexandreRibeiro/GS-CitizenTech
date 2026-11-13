// Tipos que espelham os DTOs da API Java (pacote br.com.fiap.dto).

export type Setor = "ENERGIA" | "AGUA" | "GAS" | "TELECOM" | "ENTREGA" | "OUTRO";
export type TipoSolicitacao = "VISITA" | "PIX" | "LIGACAO" | "LINK" | "MENSAGEM";
export type StatusSolicitacao = "PENDENTE" | "LIBERADA" | "BLOQUEADA";
export type StatusVisita = "AGENDADA" | "CONFIRMADA" | "CANCELADA";
export type AgendadoPor = "EMPRESA" | "GUARDIAO";

export interface Idoso {
  id: number;
  nome: string;
  cpfMascarado: string;
  telefone: string;
  bairro: string;
  dataNascimento: string;
  idade: number;
  limitePix: number;
  qtGuardioes: number;
}

export interface CadastroIdoso {
  nome: string;
  cpf: string;
  telefone: string;
  bairro: string;
  dataNascimento: string;
  limitePix: number | null;
}

export interface Guardiao {
  id: number;
  nome: string;
  cpfMascarado: string;
  telefone: string;
  email: string;
}

export interface CadastroGuardiao {
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
}

export interface Empresa {
  id: number;
  nome: string;
  cnpj: string;
  setor: Setor;
}

export interface CadastroEmpresa {
  nome: string;
  cnpj: string;
  setor: Setor | "";
}

export interface Prestador {
  id: number;
  nome: string;
  cpfMascarado: string;
  funcao: string;
  idEmpresa: number;
  nomeEmpresa: string;
}

export interface CadastroPrestador {
  idEmpresa: number | null;
  nome: string;
  cpf: string;
  funcao: string;
}

export interface Vinculo {
  idIdoso: number;
  nomeIdoso: string;
  idGuardiao: number;
  nomeGuardiao: string;
  telefoneGuardiao: string;
  parentesco: string;
  prioridade: number;
  dataVinculo: string;
}

export interface CadastroVinculo {
  idIdoso: number | null;
  idGuardiao: number | null;
  parentesco: string;
  prioridade: number;
}

export interface Visita {
  id: number;
  idIdoso: number;
  nomeIdoso: string;
  bairro: string;
  idPrestador: number | null;
  visitante: string;
  funcao: string | null;
  empresa: string | null;
  dataAgendada: string;
  codigo: string;
  agendadoPor: AgendadoPor;
  status: StatusVisita;
  descricaoStatus: string;
}

export interface CadastroVisita {
  idIdoso: number | null;
  idPrestador: number | null;
  nomeVisitante: string;
  dataAgendada: string;
  agendadoPor: AgendadoPor;
  status?: StatusVisita;
}

export interface ResultadoConferencia {
  resultado: "CONFIRMADA" | "NEGADA";
  mensagem: string;
  visitante?: string;
  funcao?: string;
  empresa?: string;
  horario?: string;
  idSolicitacao?: number;
  guardioesAvisados: string[];
}

export interface Solicitacao {
  id: number;
  idIdoso: number;
  nomeIdoso: string;
  bairro: string;
  tipo: TipoSolicitacao;
  descricaoTipo: string;
  descricao: string;
  valor: number | null;
  contatoSuspeito: string | null;
  status: StatusSolicitacao;
  descricaoStatus: string;
  orientacao: string | null;
  idGuardiaoResposta: number | null;
  nomeGuardiaoResposta: string | null;
  dataAbertura: string;
  dataResposta: string | null;
  limitePix: number;
  aprovacaoObrigatoria: boolean;
  idVisita: number | null;
  bloqueiosDoContato: number | null;
}

export interface CadastroSolicitacao {
  idIdoso: number | null;
  tipo: TipoSolicitacao;
  descricao: string;
  valor: number | null;
  contatoSuspeito: string;
}

export interface Indicadores {
  valorProtegido: number;
  golpesEvitados: number;
  tentativasNaPorta: number;
  minutosMedioResposta: number;
  idososProtegidos: number;
  idososSemGuardiao: number;
  pendentes: number;
}

export interface ValorProtegido {
  idoso: string;
  qtBloqueios: number;
  valorProtegido: number;
}

export interface TentativasBairro {
  bairro: string;
  qtTentativas: number;
}

export interface SolicitacoesPorTipo {
  tipo: TipoSolicitacao;
  total: number;
  bloqueadas: number;
  liberadas: number;
  pendentes: number;
}

export interface TempoResposta {
  guardiao: string;
  qtRespostas: number;
  minutosMedios: number;
}

export interface ContatoRecorrente {
  contato: string;
  tipo: TipoSolicitacao;
  qtIdosos: number;
  qtBloqueios: number;
  ultimoUso: string;
}

export interface IdosoSemGuardiao {
  id: number;
  nome: string;
  bairro: string;
}
