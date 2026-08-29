# 📈 PortfolioScheduler — Sistema de Compra Programada de Ações

> Sistema de **corretora de ativos** que automatiza investimentos recorrentes em uma carteira recomendada de ações, construído com **Java 21**, **Spring Boot 3**, **Hexagonal Architecture**, **CQRS** e **DDD**.

---

## 🎯 O Desafio de Negócio

Imagine que você é cliente de uma corretora de valores e quer investir todo mês em ações, mas não tem tempo ou conhecimento para escolher quais empresas comprar. A corretora oferece um produto chamado **"Top Five"**: uma carteira com as 5 melhores ações selecionadas por especialistas.

**O problema:** Como automatizar esse processo para milhares de clientes, cada um com um valor de aporte diferente, garantindo que todos recebam suas ações de forma justa e proporcional?

### Como funciona na prática?

1. **Você adere ao produto** informando quanto quer investir por mês (ex: R$ 1.500)
2. **O sistema divide seu aporte em 3 parcelas** — compras acontecem nos dias 5, 15 e 25
3. **A corretora compra as ações de forma consolidada** — junta o dinheiro de todos os clientes e faz uma única compra grande (conseguindo melhores preços)
4. **Cada cliente recebe sua parte proporcional** — se você contribuiu com 10% do total, recebe 10% das ações compradas
5. **Sobras ficam guardadas para a próxima rodada** — como ações são números inteiros, eventuais "pedaços" que não puderam ser distribuídos aguardam a próxima compra

---

## 🏦 O que é Custódia?

**Custódia** é onde suas ações ficam "guardadas" digitalmente. Pense como uma conta corrente, mas para ações ao invés de dinheiro.

| Tipo | O que é | Exemplo |
|------|---------|---------|
| **Custódia do Cliente** | Suas ações pessoais | Você tem 50 ações de PETR4 com preço médio de R$ 35,20 |
| **Custódia Master** | "Cofre" da corretora | Guarda as sobras das distribuições até a próxima compra |

Quando o sistema distribui as ações compradas, ele transfere da custódia master para a custódia de cada cliente, atualizando automaticamente o **preço médio** de cada ativo.

---

## 💰 Como Acontece a Compra?

O **motor de compra** é o coração do sistema. Três vezes por mês (dias 5, 15 e 25), ele executa automaticamente:

- **Consolidação:** O sistema soma o valor de todos os clientes ativos. Se 100 clientes investem 500 reais cada, temos 50 mil reais para comprar ações naquele dia.

- **Cálculo por ativo:** Se a carteira Top Five indica 30% em PETR4 e o preço está em 35 reais, o sistema calcula quantas ações conseguimos comprar com 15 mil reais (30% do total).

- **Compra inteligente:** Na B3 (bolsa brasileira), ações são negociadas em "lotes" de 100 unidades (mais barato) ou no "mercado fracionário" (unidades soltas). O sistema sempre prioriza lotes para economizar.

- **Distribuição justa:** Cada cliente recebe proporcionalmente ao que investiu. Se você colocou 2% do total, recebe 2% das ações.

- **Gestão de sobras:** Às vezes a conta não fecha certinho. Se compramos 350 ações mas a distribuição exata seria 350,7, as 0,7 ações de diferença ficam na custódia master e entram na próxima rodada.

---

## 🔄 Rebalanceamento de Carteira

A equipe de especialistas pode mudar a composição do Top Five — tirar uma ação que não está performando bem e colocar outra. Quando isso acontece, o sistema:

- **Vende automaticamente** as ações que saíram da carteira
- **Compra as novas** ações que entraram
- **Ajusta os percentuais** se alguma ação mudou de peso (ex: de 20% para 15%)

Tudo isso de forma transparente, sem que o cliente precise fazer nada.

---

## 📊 Funcionalidades do Sistema

- **Adesão de clientes** com valor mensal configurável
- **Gestão da carteira Top Five** — 5 ações com percentuais alvo
- **Importação de cotações da B3** — preços atualizados diariamente
- **Motor de Compra Programada** — execução automatizada nos dias 5, 15 e 25
- **Distribuição proporcional** — cada cliente recebe sua parte justa
- **Preço médio ponderado** — atualizado a cada compra
- **Rebalanceamento automático** — quando a carteira muda
- **IR Dedo-Duro** — cálculo do imposto retido na fonte (0,005%)
- **Consulta de carteira** — cliente visualiza sua posição atualizada

### IR Dedo-Duro e Outbox Pattern

O sistema calcula automaticamente o IR Dedo-Duro (0,005% sobre o valor de cada operação) e utiliza o **Outbox Pattern** para garantir a entrega confiável ao Kafka:

1. Quando uma compra é executada, o cálculo do IR é salvo na tabela `outbox` com status `PENDING`
2. Um **cron job** roda periodicamente, busca os registros pendentes e publica no tópico Kafka
3. Após publicação bem-sucedida, o status é atualizado para `PUBLISHED`

Isso garante consistência entre o banco de dados e o Kafka, evitando perda de mensagens mesmo em caso de falhas.

---

## 🏗️ Arquitetura Técnica

```
srv-portfolio-scheduler/
├── adapters/
│   ├── input/              # Controllers REST, Filters
│   ├── output/             # Repository Adapters, Mappers
│   └── mapper/             # Entity Mappers
├── application/
│   ├── command/            # Commands CQRS + Handlers
│   ├── queries/            # Queries CQRS + Handlers
│   ├── service/            # Application Services
│   └── ports/              # Input/Output Ports (interfaces)
├── domain/
│   ├── entities/           # Entidades ricas de domínio
│   ├── services/           # Domain Services
│   ├── valueObject/        # Value Objects (Money)
│   ├── enums/              # Enumerações de domínio
│   └── exceptions/         # Domain Exceptions
├── infrastructure/
│   ├── config/             # Spring Configurations
│   ├── entities/           # JPA Entities
│   └── persistence/        # JPA Repositories
└── cotahist/               # Arquivos COTAHIST diários da B3
```

### Padrões & Princípios

| Padrão | Uso |
|--------|-----|
| **Hexagonal Architecture** | Ports & Adapters com inversão de dependência |
| **CQRS Leve** | Separação de Commands (escrita) e Queries (leitura) com handlers dedicados. Usa o mesmo banco de dados, mas com queries otimizadas e cache Redis para consultas de alta performance |
| **DDD** | Entidades ricas com comportamento, Domain Services, Value Objects |
| **Repository Pattern** | Abstração de acesso a dados com Spring Data JPA |
| **Outbox Pattern** | IR Dedo-Duro via tabela outbox + Kafka |
| **Factory Method** | Criação de entidades com validação de domínio |

---

## 🛠️ Stack Tecnológica

| Componente | Tecnologia |
|------------|-----------|
| **Runtime** | Java 21 + Virtual Threads |
| **Framework** | Spring Boot 3.x |
| **Banco de Dados** | SQL Server + Spring Data JPA + Flyway |
| **Cache** | Redis (otimização de consultas) |
| **Mensageria** | Apache Kafka (IR Dedo-Duro) |
| **Documentação API** | SpringDoc OpenAPI (Swagger) |
| **Cotações** | Arquivo COTAHIST da B3 |
| **Testes** | JUnit 5 + Mockito |

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21+
- Docker e Docker Compose (recomendado)
- Arquivo(s) COTAHIST da B3 na pasta `cotahist/`

### Executando com Docker Compose

O projeto inclui um `docker-compose.yml` que sobe SQL Server, Redis e Kafka:

```bash
# Clonar o repositório
git clone https://github.com/matheusmaiagoulart/srv-portfolio-scheduler.git
cd srv-portfolio-scheduler

# Subir a infraestrutura (SQL Server, Redis, Kafka)
docker-compose up -d

# Executar a aplicação
./mvnw spring-boot:run
```

> ⚠️ **Importante:** As configurações de conexão estão em `application.yml` usando variáveis de ambiente. Ajuste conforme seu ambiente local se necessário.

Swagger disponível em `http://localhost:8080/swagger-ui.html`

### Testes

```bash
./mvnw test
```

---

## 📂 Arquivos COTAHIST da B3

O sistema importa cotações diárias da B3 (Bolsa de Valores) através de arquivos COTAHIST:

```
cotahist/
├── COTAHIST_D27032026.TXT
├── COTAHIST_D28032026.TXT
└── ...
```

Download: [Cotações Históricas B3](https://www.b3.com.br/pt_br/market-data-e-indices/servicos-de-dados/market-data/historico/mercado-a-vista/cotacoes-historicas/)

---

## 📋 Status das Funcionalidades

| Funcionalidade | Status |
|----------------|--------|
| Adesão de clientes | ✅ |
| Gestão da carteira Top Five | ✅ |
| Importação de cotações (COTAHIST) | ✅ |
| Motor de Compra Programada | ✅ |
| Distribuição proporcional | ✅ |
| Gestão de resíduos | ✅ |
| Preço médio ponderado | ✅ |
| Rebalanceamento de carteira | ✅ |
| IR Dedo-Duro (Kafka) | ✅ |
| Cache Redis | ✅ |
| Testes unitários | ✅ |
| Docker Compose | ✅ |

---

## 🎓 Créditos

Este projeto é uma implementação do desafio técnico criado por **Guilherme Marques Camarão**.

- 🔗 [LinkedIn do Guilherme Camarão](https://www.linkedin.com/in/guilherme-camarao/)
- 📁 [Repositório do Desafio Original](https://github.com/gcamarao/teste_itau_v2)

---

## 📄 Licença

Projeto educacional e de portfólio.
