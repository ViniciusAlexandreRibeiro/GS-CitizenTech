import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { viteSingleFile } from "vite-plugin-singlefile";

// O build gera um único index.html (JS e CSS embutidos) na pasta ../web.
// Assim o arquivo abre com duplo clique, sem precisar de servidor.
export default defineConfig({
  base: "./",
  plugins: [react(), tailwindcss(), viteSingleFile()],
  build: {
    outDir: "../web",
    emptyOutDir: true,
  },
  server: {
    port: 5173,
  },
});
