# Gestão Financeira

API REST em Spring Boot 4.1 / Java 21 com MySQL e autenticação JWT.

## Como rodar

```bash
export DB_PASSWORD=sua_senha      # DB_USER padrão: root
export JWT_SECRET=uma-chave-longa-com-mais-de-32-caracteres
./mvnw spring-boot:run
```

Banco esperado: `gestao_financeira` em `localhost:3306` (`ddl-auto=validate`, então as tabelas precisam existir).

## Estrutura

Organizado **por funcionalidade**: cada módulo em `src/main/java/com/gestaofinanceira/` tem as mesmas camadas
(`controller` → `service` → `repository` → `entity`, mais `dto`).

| Módulo        | O que faz                                   |
|---------------|---------------------------------------------|
| `auth`        | Login e geração do token JWT                |
| `usuario`     | Cadastro e atualização de usuários          |
| `categoria`   | Categorias de receita/despesa               |
| `transacao`   | Lançamentos (receitas e despesas)           |
| `orcamento`   | Orçamento por categoria                     |
| `meta`        | Metas financeiras                           |
| `alerta`      | Alertas                                     |
| `dashboard`   | Resumo e gastos por categoria               |
| `comparativo` | Variação entre períodos                     |
| `relatorio`   | Relatório mensal                            |
| `historico`   | Histórico de alterações das transações      |
| `importacao`  | Importação de extrato bancário (CSV)        |
| `config`      | Segurança, filtro e serviço JWT             |
| `exception`   | Exceções e tratamento global de erros       |
