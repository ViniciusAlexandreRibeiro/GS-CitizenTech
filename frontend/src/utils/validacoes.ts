// Validações feitas na página antes de enviar para a API.
// A API valida tudo de novo (regras R1 a R13 do plano).

export const somenteDigitos = (valor: string) => valor.replace(/\D/g, "");

export function cpfValido(cpf: string): boolean {
  const d = somenteDigitos(cpf);
  if (d.length !== 11 || /^(\d)\1+$/.test(d)) return false;
  const digito = (qtd: number) => {
    let soma = 0;
    for (let i = 0; i < qtd; i++) soma += Number(d[i]) * (qtd + 1 - i);
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };
  return digito(9) === Number(d[9]) && digito(10) === Number(d[10]);
}

export function cnpjValido(cnpj: string): boolean {
  const d = somenteDigitos(cnpj);
  if (d.length !== 14 || /^(\d)\1+$/.test(d)) return false;
  const digito = (pesos: number[]) => {
    const soma = pesos.reduce((total, peso, i) => total + Number(d[i]) * peso, 0);
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  return (
    digito([5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) === Number(d[12]) &&
    digito([6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) === Number(d[13])
  );
}

export const telefoneValido = (tel: string) => [10, 11].includes(somenteDigitos(tel).length);

export const emailValido = (email: string) => /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(email.trim());

export function idadeEmAnos(dataIso: string): number {
  const nascimento = new Date(dataIso + "T00:00:00");
  const hoje = new Date();
  let idade = hoje.getFullYear() - nascimento.getFullYear();
  const aindaNaoFezAniversario =
    hoje.getMonth() < nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() && hoje.getDate() < nascimento.getDate());
  if (aindaNaoFezAniversario) idade--;
  return idade;
}

export const dataHoraNoFuturo = (valor: string) => new Date(valor).getTime() > Date.now();

/** Objeto com a mensagem de erro de cada campo (vazio = formulário válido). */
export type Erros = Record<string, string>;

export const temErros = (erros: Erros) => Object.keys(erros).length > 0;
