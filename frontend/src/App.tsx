import { Route, Routes } from "react-router";
import { Footer } from "./components/Footer";
import { Header } from "./components/Header";
import { useUsuario } from "./context/UsuarioContext";
import { Cadastros } from "./pages/Cadastros";
import { Impacto } from "./pages/Impacto";
import { Inicio } from "./pages/Inicio";
import { NaoEncontrada } from "./pages/NaoEncontrada";
import { PainelGuardiao } from "./pages/PainelGuardiao";
import { Pedido } from "./pages/Pedido";
import { Porta } from "./pages/Porta";
import { Visitas } from "./pages/Visitas";

export default function App() {
  const { erroApi, recarregarPessoas } = useUsuario();

  return (
    <div className="flex min-h-screen flex-col">
      <a href="#conteudo" className="sr-only focus:not-sr-only focus:absolute focus:left-2 focus:top-2 focus:z-50 focus:rounded focus:bg-white focus:p-2">
        Pular para o conteúdo
      </a>
      <Header />
      {erroApi && (
        <div role="alert" className="bg-red-700 px-4 py-2 text-center text-white">
          {erroApi}{" "}
          <button className="font-bold underline" onClick={() => recarregarPessoas()}>Tentar de novo</button>
        </div>
      )}
      <div id="conteudo" className="flex-1">
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/porta" element={<Porta />} />
          <Route path="/pedido" element={<Pedido />} />
          <Route path="/guardiao" element={<PainelGuardiao />} />
          <Route path="/visitas" element={<Visitas />} />
          <Route path="/cadastros" element={<Cadastros />} />
          <Route path="/impacto" element={<Impacto />} />
          <Route path="*" element={<NaoEncontrada />} />
        </Routes>
      </div>
      <Footer />
    </div>
  );
}
