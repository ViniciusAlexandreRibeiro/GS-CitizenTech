// Ícones simples em SVG (sem biblioteca externa).
const caminhos: Record<string, string> = {
  escudo: "M12 2l8 3v6c0 5-3.5 9-8 11-4.5-2-8-6-8-11V5z M8.5 12l2.5 2.5 4.5-5",
  porta: "M5 21V4a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v17 M3 21h18 M15 12h.01",
  dinheiro: "M2 6h20v12H2z M12 9.4a2.6 2.6 0 1 0 0 5.2 2.6 2.6 0 0 0 0-5.2 M6 10v4 M18 10v4",
  mensagem: "M21 12a8 8 0 0 1-11.6 7.1L4 20l1.1-4.6A8 8 0 1 1 21 12z M12 8v4 M12 15.5h.01",
  check: "M5 12.5l4.5 4.5L19 7.5",
  x: "M6 6l12 12 M18 6L6 18",
  alerta: "M12 3l10 18H2z M12 10v5 M12 18h.01",
  telefone: "M5 3h4l2 5-3 2a12 12 0 0 0 6 6l2-3 5 2v4a2 2 0 0 1-2 2A18 18 0 0 1 3 5a2 2 0 0 1 2-2z",
  menu: "M4 7h16 M4 12h16 M4 17h16",
  voltar: "M15 5l-7 7 7 7",
  relogio: "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18 M12 7v5l3 2",
};

interface Props {
  nome: keyof typeof caminhos;
  tamanho?: number;
  className?: string;
}

export function Icone({ nome, tamanho = 22, className }: Props) {
  return (
    <svg width={tamanho} height={tamanho} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2.1}
      strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className={className}>
      <path d={caminhos[nome]} />
    </svg>
  );
}
