import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { HashRouter } from "react-router";
import App from "./App";
import { UsuarioProvider } from "./context/UsuarioContext";
import "./index.css";

// HashRouter (#/visitas) porque o index.html é aberto direto do arquivo, sem servidor.
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <HashRouter>
      <UsuarioProvider>
        <App />
      </UsuarioProvider>
    </HashRouter>
  </StrictMode>,
);
