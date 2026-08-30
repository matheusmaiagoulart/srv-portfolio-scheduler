# 🏗️ Decisões de Arquitetura — Perguntas e Respostas

Este documento reúne as principais decisões arquiteturais do projeto, explicando o **porquê** de cada escolha, os **trade-offs** envolvidos e os **caminhos de evolução** futuros.

---

## 📚 Índice

1. [Por que Arquitetura Hexagonal?](#-por-que-arquitetura-hexagonal)
2. [Por que CQRS?](#-por-que-cqrs)
3. [Por que DDD?](#-por-que-ddd)
4. [Trade-offs e Pontos de Evolução](#-trade-offs-e-pontos-de-evolução)

---

## 🔷 Por que Arquitetura Hexagonal?

**Para proteger o domínio de detalhes de infraestrutura.**

Este é um domínio denso, com muitas regras de negócio: cálculo de distribuição proporcional, preço médio ponderado, rebalanceamento de carteira, split entre lote padrão e fracionário, IR Dedo-Duro, etc.

A Arquitetura Hexagonal (Ports & Adapters) nos permite **evoluir a infraestrutura sem quebrar a lógica de domínio**. Se amanhã precisar trocar SQL Server por PostgreSQL, ou Redis por Memcached, o domínio permanece intocado — só troco os adapters.

O domínio fica no centro, isolado, e a infraestrutura se conecta através de **Ports** (interfaces) e **Adapters** (implementações).

---

## 🔷 Por que CQRS?

**Porque neste domínio, leituras são muito mais frequentes que escritas.**

Analisando o desafio numa aplicação real:

- **Milhares de usuários** acessando sua carteira diariamente, às vezes mais de uma vez ao dia
- **Poucos comandos de escrita**: adesão de novos clientes (esporádico), execução de compra (3 vezes ao mês)

O CQRS se encaixa perfeitamente nesse cenário:

- **Queries otimizadas** + cache Redis para as milhares de operações de leitura
- **Commands separados** para as operações de escrita, com toda a lógica de negócio encapsulada
- **Handlers dedicados** para cada operação, com responsabilidade única

Implementei um **CQRS Leve** (mesmo banco, handlers separados) porque o volume atual não justifica a complexidade de bancos separados com consistência eventual. Mas a estrutura permite evoluir incrementalmente se precisar.

---

## 🔷 Por que DDD?

**Porque é um domínio que se conversa muito, com muitas entidades interdependentes.**

Manter **entidades ricas** com seus próprios métodos de lógica, usando **nomenclaturas do negócio** (Custódia, Preço Médio, Lote Padrão, Carteira Recomendada), ajuda a:

- **Não perder contexto de negócio no código** — o código expressa o domínio
- **Melhor manutenabilidade** — novos desenvolvedores entendem o negócio lendo o código
- **Comportamento encapsulado** — `custody.addQuantity()` sabe calcular preço médio, não precisa de um service externo

---

## 🔷 Trade-offs e Pontos de Evolução

### ⚠️ Limitações Atuais

| Aspecto | Situação Atual | Impacto |
|---------|----------------|---------|
| **Processamento em Batch** | Sequencial (1000 clientes por vez) | Tempo de execução O(n) — linear |
| **Arquitetura** | Monolito | Cron, compra, distribuição e rebalanceamento no mesmo serviço |
| **CQRS** | Leve (mesmo banco) | Sem separação física, consistência forte |

### 🚀 Evoluções Futuras

#### 1. Processamento Paralelo

Atualmente o batch processa sequencialmente: busca 1000, processa, busca mais 1000. O tempo é **O(n)**.

**Evolução:** Implementar processamento paralelo com múltiplas threads/workers. Cada batch poderia ser processado em paralelo, reduzindo tempo para **O(n/k)** onde k é o número de workers.

#### 2. Separação em Microserviços

Hoje é um monolito. Poderia ser separado em pelo menos **3 serviços**:

| Serviço | Responsabilidade |
|---------|------------------|
| **API Principal** | Receber requests (adesão, consultas, carteira) |
| **Worker de Compra** | Processar compra, distribuição, rebalanceamento |
| **Cron Service** | Agendar execuções, publicar outbox no Kafka |

Benefícios: escalabilidade independente, deploy isolado, resiliência.

#### 3. CQRS Completo

Se o número de clientes escalar muito (milhões), evoluir para CQRS com **separação física de banco**:

| Banco | Uso | Característica |
|-------|-----|----------------|
| **Write DB** | Commands | Consistência forte, modelo normalizado |
| **Read DB** | Queries | Réplica otimizada, desnormalizada, cache |

Com **consistência eventual** — aceitar delay de alguns segundos entre escrita e leitura. Isso permite escalar leituras horizontalmente sem impactar escritas.

---

## 📊 Resumo

| Decisão | Motivação Principal |
|---------|---------------------|
| **Hexagonal** | Proteger domínio denso de detalhes de infraestrutura |
| **CQRS** | Leituras >>> escritas neste domínio |
| **DDD** | Domínio complexo com muitas entidades que se conversam |
| **Monolito inicial** | Simplicidade para MVP, estrutura preparada para evoluir |

A arquitetura foi pensada para ser **pragmática**: resolve o problema atual sem over-engineering, mas com estrutura que permite evoluir quando (e se) necessário.

---

## 🔷 Por que Outbox Pattern?

**Porque são muitos usuários envolvidos no cálculo de imposto de renda.**

Para garantir a entrega de todos os registros de IR, é mais seguro salvar os dados que serão enviados **junto da transação principal** e garantir que o IR foi calculado corretamente — para posteriormente enviar ao Kafka.

A alternativa seria enviar diretamente ao Kafka durante a transação. Mas se o broker estiver indisponível:

- Ou deixamos de enviar informação de IR de algum usuário (dados fiscais perdidos)
- Ou cancelamos a transação de **todos** por causa de 1 falha

Com o Outbox Pattern, a transação principal sempre persiste. Um cron job separado lê os registros pendentes e publica no Kafka. Se falhar, tenta novamente até conseguir.

---

## 🔷 Por que Java 21 + Virtual Threads?

**Para escalar o número de requisições que a aplicação suporta.**

Virtual Threads permitem criar milhares de threads "leves" sem o overhead de threads de sistema operacional. Isso conversa muito bem com o domínio CQRS com muitas leituras — cada request de consulta de carteira pode ter sua própria virtual thread sem problema.

### ⚠️ Consideração

Dependendo do número de requisições que vão ao banco, pode trazer complicações relacionadas ao I/O no banco de dados (pool de conexões saturado, por exemplo).

**Mas o cache está aí para reduzir essa possibilidade** — a maioria das leituras nem chega ao banco. Portanto, é necessário analisar a quantidade de requisições que a aplicação teria para ajustar as estratégias (pool size, cache TTL, etc).

---

## 🔷 Tratamento de Erros

**Erros logam e cancelam a transação.**

Um trade-off atual é que a distribuição está envolta em **apenas 1 transação principal** para garantir ACID. Se um erro ocorrer no final do processamento, todos os outros processamentos feitos são desfeitos e não persistidos.

### ⚠️ Problema Atual

Se processar 999 clientes com sucesso e o 1000º falhar, tudo é revertido.

### 🚀 Evolução

O paralelismo com **controle por tabela de status de cada batch** virá para ajudar nessa questão. Cada batch teria seu próprio controle transacional, permitindo que falhas isoladas não afetem os demais.

---

## 🔷 Estratégia de Cache

**Cache nas operações de consulta de carteira do usuário e histórico de cestas.**

### Invalidação

A carteira do usuário é **invalidada no momento que a compra começa a rodar**. Assim, na próxima vez que o usuário acessar após a compra, ele vai ao banco e traz os dados atualizados.

### Por que funciona bem?

Os dados dos ativos do cliente **só mudam quando uma compra acontece** (3 vezes ao mês). Entre as compras, os dados são estáveis e podem ficar em cache indefinidamente sem risco de inconsistência.

```
Dia 5 → Compra executada → Cache invalidado → Usuário consulta → Cache populado
...
(dias 6-14: cache servindo todas as consultas)
...
Dia 15 → Compra executada → Cache invalidado → ...
```
