import { somenteDigitos } from "./validacoes";

const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export const formatarMoeda = (valor: number | null | undefined) => (valor == null ? "—" : moeda.format(valor));

export function formatarDataHora(iso: string | null | undefined): string {
  if (!iso) return "—";
  const data = new Date(iso);
  return data.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" });
}

export function formatarData(iso: string | null | undefined): string {
  if (!iso) return "—";
  return new Date(iso.length === 10 ? iso + "T00:00:00" : iso).toLocaleDateString("pt-BR");
}

/** "há 5 min", "há 2 h", "ontem"... */
export function tempoRelativo(iso: string): string {
  const minutos = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
  if (minutos < 1) return "agora";
  if (minutos < 60) return `há ${minutos} min`;
  const horas = Math.round(minutos / 60);
  if (horas < 24) return `há ${horas} h`;
  const dias = Math.round(horas / 24);
  return dias === 1 ? "ontem" : `há ${dias} dias`;
}

export function mascaraCpf(valor: string): string {
  return somenteDigitos(valor)
    .slice(0, 11)
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

export function mascaraCnpj(valor: string): string {
  return somenteDigitos(valor)
    .slice(0, 14)
    .replace(/(\d{2})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1/$2")
    .replace(/(\d{4})(\d{1,2})$/, "$1-$2");
}

export function mascaraTelefone(valor: string): string {
  const d = somenteDigitos(valor).slice(0, 11);
  if (d.length <= 10) return d.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{4})(\d{1,4})$/, "$1-$2");
  return d.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{5})(\d{1,4})$/, "$1-$2");
}

/** Telefone guardado só com dígitos -> (11) 98877-1234. Links e chaves ficam como estão. */
export function formatarContato(contato: string | null): string {
  if (!contato) return "—";
  if (/^0[38]00\d{7}$/.test(contato)) return contato.replace(/^(\d{4})(\d{3})(\d{4})$/, "$1 $2 $3"); // 0800 721 0000
  return /^\d{10,11}$/.test(contato) ? mascaraTelefone(contato) : contato;
}

/** Data/hora local no formato do input datetime-local: 2026-11-20T14:00 */
export function paraInputDataHora(data: Date): string {
  const p = (n: number) => String(n).padStart(2, "0");
  return `${data.getFullYear()}-${p(data.getMonth() + 1)}-${p(data.getDate())}T${p(data.getHours())}:${p(data.getMinutes())}`;
}

export const NOMES_TIPO: Record<string, string> = {
  VISITA: "Alguém na porta",
  PIX: "Pedido de dinheiro",
  LIGACAO: "Ligação",
  LINK: "Link",
  MENSAGEM: "Mensagem",
};

export const NOMES_SETOR: Record<string, string> = {
  ENERGIA: "Energia",
  AGUA: "Água",
  GAS: "Gás",
  TELECOM: "Telecom / internet",
  ENTREGA: "Entregas",
  OUTRO: "Outro",
};
