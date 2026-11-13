# Guardião

**Global Solution 2026/2 · CitizenTech · FIAP**

Golpes contra idosos quase sempre seguem o mesmo roteiro: alguém pressiona a pessoa a decidir sozinha e na hora, seja abrindo a porta para um "técnico da companhia de luz" ou fazendo um Pix para um "neto com número novo".

O **Guardião** cria uma rede de confiança entre o idoso e a família:

- **Na porta:** empresas e familiares agendam a visita e recebem um código de 6 dígitos. O idoso digita o código e o sistema responde **PODE ABRIR** ou **NÃO ABRA**. Código errado avisa os guardiões na hora.
- **Pedidos de dinheiro, ligações e links:** o idoso registra o que aconteceu e os guardiões (filhos, netos, vizinhos) respondem **pode seguir** ou **é golpe**. O sistema também avisa se aquele número já foi bloqueado por outras famílias.

**ODS atendidos:** 3 (saúde e bem-estar), 9 (inovação), 10 (redução das desigualdades) e 11 (cidades e comunidades seguras).

---

## Como executar

**Pré-requisitos:** JDK 21 ou superior, Maven (já vem no IntelliJ), acesso ao Oracle da FIAP e SQL Developer.

1. **Banco:** no SQL Developer, abra `database/guardiao.sql` e execute com **F5**.
   Se as tabelas já existirem, rode antes o `database/limpar-banco.sql`.

2. **Configuração:** em `backend/`, copie `db.properties.example` para `db.properties` e preencha com seu usuário e senha do Oracle.

3. **API:** abra a pasta `backend` no IntelliJ e execute a classe `br.com.fiap.Main`
   (ou, no terminal, dentro de `backend/`: `mvn compile exec:java`).
   A API sobe em `http://localhost:8080`. Para conferir: `http://localhost:8080/status` deve mostrar `"banco":"ok"`.

4. **Interface:** abra o arquivo `web/index.html` no navegador (duplo clique). Na tela inicial, escolha quem está usando.

**Para testar a demonstração:** entre como *Maria Aparecida Souza*, vá em **Porta** e digite `482913` (visita agendada para hoje às 14h, vale das 12h às 16h). Depois digite `000000` para ver o alerta. Em seguida, entre como *Ana Souza* e responda o pedido no **Painel**.

---

## Funcionalidades

| Página | O que faz |
|---|---|
| Início | Escolha do perfil e, para o idoso, três botões: alguém na porta, pediram dinheiro, recebi algo estranho |
| Porta | Confere o código da visita e mostra quem é o visitante ou o alerta de "não abra" |
| Pedir ajuda | Registra um pedido (Pix, ligação, link ou mensagem) e avisa se o contato já foi bloqueado |
| Painel | O guardião vê os pedidos pendentes e decide: bloquear ou pode seguir |
| Visitas | Agendar (gera o código), remarcar, cancelar e excluir |
| Cadastros | CRUD de idosos, guardiões, vínculos, empresas e prestadores |
| Impacto | Relatórios: valor protegido, tentativas na porta por bairro, pedidos por tipo, tempo de resposta |

**Regras principais:** CPF e CNPJ validados pelos dígitos verificadores; idoso com 60 anos ou mais; até 5 guardiões por idoso; o código da visita só vale 2 horas antes ou depois do horário; Pix acima do limite do idoso exige aprovação; só um guardião vinculado ao idoso pode responder, e uma única vez.

---

## Tecnologias

| Parte | Tecnologia |
|---|---|
| Back-end | Java 21+, Maven, JDBC, API REST com JAX-RS (Jersey + Grizzly), JUnit 5 |
| Banco | Oracle (servidor da FIAP) |
| Front-end | React + TypeScript (Vite) e Tailwind CSS |

## Estrutura

```
database/      guardiao.sql (tabelas, carga e relatórios) e limpar-banco.sql
backend/       API em Java
  models/        entidades (Pessoa, Idoso, Guardiao, Visita, SolicitacaoVerificacao...) e enums
  dao/           acesso ao banco (JDBC)
  service/       regras de negócio
  resource/      endpoints da API
  dto/           dados que entram e saem da API
  exception/     erros do sistema e respostas de erro em JSON
  db/            ConnectionFactory
frontend/      código-fonte da interface (React)
web/           interface pronta para abrir no navegador
diagramas/     diagrama ER e diagrama de classes
docs/          api.http (exemplos de requisições) e Apresentacao-Projeto-Guardiao.pdf
```

## Banco de dados

7 tabelas: `T_IDOSO`, `T_GUARDIAO`, `T_VINCULO` (associativa N:N entre idoso e guardião), `T_EMPRESA`, `T_PRESTADOR`, `T_VISITA` e `T_SOLICITACAO`.
O script cria as tabelas com PK, FK, UNIQUE e CHECK, insere dados de exemplo e termina com os 6 relatórios (JOIN, GROUP BY, HAVING, subconsulta e LEFT JOIN) usados na página de Impacto.

## API

Base `http://localhost:8080`, sempre em JSON. Exemplos prontos em `docs/api.http`.

| Recurso | Rotas |
|---|---|
| `/idosos`, `/guardioes`, `/empresas`, `/prestadores` | `GET`, `POST`, `GET/PUT/DELETE /{id}` |
| `/vinculos` | `GET`, `POST`, `GET/PUT/DELETE /{idIdoso}/{idGuardiao}` |
| `/visitas` | CRUD + `POST /visitas/conferir` |
| `/solicitacoes` | CRUD + `PATCH /solicitacoes/{id}/resposta` + `GET /solicitacoes/bloqueios?contato=` |
| `/relatorios` | `/indicadores`, `/valor-protegido`, `/porta-por-bairro`, `/por-tipo`, `/tempo-resposta`, `/contatos-recorrentes`, `/idosos-sem-guardiao` |

Erros sempre voltam com uma mensagem clara, por exemplo:
`{ "status": 409, "erro": "Conflito", "mensagem": "Já existe um cadastro com este CPF.", "campo": "cpf" }`

## Problemas comuns

| Mensagem | O que fazer |
|---|---|
| Credenciais do banco não configuradas | Criar o `backend/db.properties` (passo 2) |
| Usuário ou senha do banco incorretos | Conferir RM e senha no `db.properties` |
| As tabelas do Guardião não existem | Rodar o `database/guardiao.sql` com o mesmo usuário |
| "Servidor indisponível" na página | A API não está rodando (passo 3) |

## Para alterar a interface

Requer Node.js 22 ou superior. Dentro de `frontend/`: `npm install`, depois `npm run dev` para desenvolver e `npm run build` para gerar o `web/index.html` de novo.

## Apresentação

O arquivo `docs/Apresentacao-Projeto-Guardiao.pdf` reúne o problema em números (com fontes), a validação por pesquisa, as telas do sistema e os próximos passos.

## Integrantes

Ver `integrantes.txt`.
