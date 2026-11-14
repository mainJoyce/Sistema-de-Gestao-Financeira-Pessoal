# FinanceIA | Back-end (Spring Boot 4, Java 21, MySQL)

Sistema de gestão financeira pessoal. Monólito modular: um projeto só, organizado por assunto.
Cada módulo tem `controller`, `service`, `repository`, `entity` e `dto`.

## Estrutura (uma pasta de projeto só)

```
gestaofinanceira/            <- abra ESTA pasta no VS Code (ela tem o pom.xml)
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
├── database/                <- scripts SQL (rode na ordem)
├── docs/ALTERACOES.md       <- o que foi corrigido e por quê
└── src/main/java/com/gestaofinanceira/
    ├── GestaoFinanceiraApplication.java
    ├── config/              (segurança, JWT)       dono: Anne
    ├── exception/           (erros padronizados)   dono: Anne
    ├── auth/  usuario/  categoria/  transacao/  historico/  importacao/
    ├── dashboard/  relatorio/  comparativo/
    ├── renda/  orcamento/  meta/  alerta/
    └── score/  insight/  recomendacao/
```

## 1. Banco de dados (MySQL 8.0.16 ou superior)

1. No DBeaver, conecte no MySQL (host `localhost`, porta `3306` ou a sua, usuário `root`).
2. Rode `database/01_criar_banco_e_tabelas.sql` (Alt + X).
   Se você já criou as tabelas antes, rode `database/02_ajustes_banco_existente.sql`.
3. Rode `database/03_categorias_padrao.sql`.
4. (Opcional) `database/04_teste_banco_financeira.sql` cria dados de teste e consultas de verificação.

## 2. Variáveis de ambiente (uma vez por computador)

No PowerShell, **um comando por vez**:

```
setx DB_PASSWORD "sua_senha_do_mysql"
setx DB_PORT "3307"          (só se o seu MySQL NÃO estiver na 3306)
```

Feche o VS Code por completo e abra de novo. Opcionais: `DB_USER` (padrão `root`) e `JWT_SECRET`.

## 3. Rodar

```
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

Esperado no fim: `Started GestaoFinanceiraApplication`. Deixe **um terminal só** rodando
(senão: "Port 8080 was already in use").

## 4. Endpoints (todos exigem token, menos os dois primeiros)

**Conta**
- `POST /api/usuarios` cadastro · `POST /api/auth/login` login (devolve o JWT)
- `GET /api/usuarios/me` · `PUT /api/usuarios/me` perfil

**Movimentações**
- `/api/categorias`, `/api/transacoes`: CRUD. `GET /api/transacoes` aceita filtros e paginação
  (`pagina`, `tamanho`; padrão 0 e 100, máximo 500).
- `GET /api/transacoes/sugestao-categoria?estabelecimento=Netflix`: sugere a categoria.
  Despesa sem categoria recebe uma automaticamente (histórico, palavra-chave ou "Outros").
- `POST /api/importacoes` (CSV) · `GET /api/importacoes`
- `GET /api/historicos`

**Planejamento**
- `/api/rendas`, `/api/orcamentos`, `/api/metas`: CRUD (`PATCH /api/metas/{id}/aporte`)
- `GET /api/reserva-emergencia?mesesCobertura=6&prazoMeses=12`

**Acompanhamento**
- `GET /api/dashboard?mes=2026-10` · `GET /api/dashboard/evolucao?meses=6`
  (inclui economia acumulada) · `GET /api/dashboard/estabelecimentos?mes=2026-10`
- `GET /api/comparativos` (mês a mês) · `GET /api/comparativos/periodos?inicioAtual=...&fimAtual=...&inicioAnterior=...&fimAnterior=...`
- `POST /api/relatorios?mes=2026-10` · `GET /api/relatorios`

**Inteligência**
- `GET /api/score` · `GET /api/score/historico`
- `GET /api/alertas` · `PATCH /api/alertas/{id}/lida` · `POST /api/alertas/gerar`
  (os alertas também são criados sozinhos quando uma transação é registrada)
- `POST /api/insights/gerar` · `GET /api/insights` · `GET /api/insights/explicacao?mes=2026-10`
- `POST /api/recomendacoes/gerar` · `GET /api/recomendacoes` · `GET /api/recomendacoes/plano` (50/30/20)
  · `GET /api/recomendacoes/patrimonio?meses=12`

IA (opcional): defina `GEMINI_API_KEY` para o texto de `/api/insights/explicacao` ser reescrito pelo Gemini.
Sem a chave, o sistema usa textos gerados por regras. O backend envia só totais agregados.

Testes automatizados (sem banco): `.\mvnw.cmd test`

## 5. Regras do projeto (siga ao criar código novo)

1. O id do usuário vem do token (`@AuthenticationPrincipal Long usuarioId`), nunca do body nem da URL.
2. Toda consulta filtra por `usuarioId`. Dado de outro usuário devolve 404.
3. DTO na API, nunca Entity. `Request` sem `id`/`usuarioId`; `Response` sem dado sensível.
4. Entity confere campo a campo com o SQL (`ddl-auto=validate` reclama se não bater).
5. Dinheiro em `BigDecimal`. Enums gravados como texto em MAIÚSCULAS.
6. Um módulo usa outro pelo **Service**, não pelo Repository.
7. Erros: lance `RecursoNaoEncontradoException` (404), `RegraDeNegocioException` (422) ou
   `ConflitoException` (409). **Nunca** `RuntimeException` ou `IllegalArgumentException` crus (viram 500).
8. `POST` devolve 201 + `Location`; `DELETE` devolve 204.
9. Não mexer em `config/`, `exception/` nem `application.properties` sem combinar com a Anne.
