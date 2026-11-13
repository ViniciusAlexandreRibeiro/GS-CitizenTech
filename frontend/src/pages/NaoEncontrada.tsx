import { Link } from "react-router";
import { Pagina } from "../components/Ui";

export function NaoEncontrada() {
  return (
    <Pagina titulo="Página não encontrada">
      <p>
        Este endereço não existe. <Link to="/" className="font-bold text-marca-700 underline">Voltar ao início</Link>.
      </p>
    </Pagina>
  );
}
