# 💰 FinanceAI — Gestão Financeira Pessoal

O **FinanceAI** é uma aplicação web desenvolvida para auxiliar os usuários no controle e planejamento de suas finanças pessoais. O sistema busca facilitar o acompanhamento de receitas, despesas e orçamentos, oferecendo uma visão mais clara da saúde financeira.

## Objetivo

Desenvolver uma plataforma que ajude os usuários a organizar suas finanças, acompanhar seus gastos e tomar decisões financeiras mais conscientes.

## Funcionalidades

- **Gestão financeira:** cadastro e acompanhamento de receitas e despesas.
- **Categorias:** organização dos gastos por categorias financeiras.
- **Dashboard:** visualização de receitas, despesas e saldo disponível.
- **Orçamentos:** definição de limites de gastos e acompanhamento do consumo.
- **Relatórios:** análise e comparação de informações financeiras.
- **Metas financeiras:** planejamento e acompanhamento de objetivos.
- **Alertas e insights:** identificação de gastos excessivos e sugestões de melhoria.
- **Saúde financeira:** indicadores para auxiliar no planejamento e controle do dinheiro.

*Observação: as funcionalidades estão sujeitas ao andamento do desenvolvimento.*

## Tecnologias

**Frontend**
- HTML5
- CSS3
- JavaScript

**Backend planejado**
- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT

**Banco de dados planejado**
- PostgreSQL

**Ferramentas**
- Git e GitHub
- Maven
- Postman

## Arquitetura

O backend seguirá uma arquitetura em camadas, com uma API REST responsável pela comunicação entre a interface e o banco de dados.

```text
Frontend
   ↓
API REST — Spring Boot
   ↓
Controllers
   ↓
Services
   ↓
Repositories
   ↓
PostgreSQL
```

## Regras de negócio

O sistema deverá respeitar algumas regras fundamentais:

- O valor das transações deve ser maior que zero.
- Cada transação deve pertencer a um usuário.
- O saldo é calculado pela diferença entre receitas e despesas.
- Os orçamentos acompanham os gastos por categoria.
- Os dados financeiros devem ser protegidos.
- Os indicadores e relatórios devem utilizar informações válidas.

## Etapas de desenvolvimento

- [ ] Estrutura inicial do projeto.
- [ ] Cadastro e autenticação de usuários.
- [ ] Gestão de receitas e despesas.
- [ ] Categorias financeiras.
- [ ] Controle de orçamentos.
- [ ] Dashboard financeiro.
- [ ] Relatórios.
- [ ] Metas financeiras.
- [ ] Alertas e insights inteligentes.
- [ ] Testes e melhorias de segurança.

## Segurança

O projeto prevê autenticação de usuários, proteção de dados financeiros, validação de informações e controle de acesso para garantir que cada usuário visualize apenas seus próprios dados.

## Equipe

**Integrantes:** Anne Vitoria, Guilherme de Franca, Joyce Stefany, Nayelle Fonseca, Rillary Luise e Sidney Miguel

**Instituição:** Universidade Tiradentes - PE

## Licença

A licença do projeto será definida posteriormente.

---

**FinanceAI — Organize seu dinheiro, planeje seu futuro.**
