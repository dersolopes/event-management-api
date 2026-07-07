# Event Manager API 🚀

API REST para gerenciamento de eventos corporativos (Summits, Feiras de Tecnologia e Congressos), desenvolvida com Spring Boot 3 e persistência em PostgreSQL gerenciada via Docker.

---

## 🛠️ Tecnologias e Ferramentas
* **Java 21** & **Spring Boot 3**
* **Spring Security** & **JWT** (Autenticação e Autorização)
* **PostgreSQL** (Banco de dados relacional)
* **Flyway** (Migração e versionamento de banco)
* **Docker** & **Docker Compose** (Containerização do ambiente)
* **MapStruct** & **Lombok** (Produtividade e mapeamento de DTOs)

---

## 📐 Arquitetura e Organização do Código
O projeto adota o padrão de empacotamento por camadas, isolando responsabilidades e facilitando testes:
* `config/`: Configurações globais do ecossistema Spring.
* `controller/`: Camada de exposição dos endpoints HTTP (consome e retorna apenas DTOs).
* `dto/`: Objetos de transferência de dados e payloads.
* `entity/`: Entidades JPA representando o modelo relacional.
* `repository/`: Interfaces de acesso a dados (Spring Data JPA).
* `service/`: Camada onde residem as regras de negócio e validações.
* `security/`: Regras de filtros JWT e controle de acessos (RBAC).
* `exception/`: Tratamento global de erros (`GlobalExceptionHandler`).
Obs.: Para entender o histórico e as justificativas das decisões técnicas tomadas neste projeto, acesse os [Registros de Decisão Arquitetural (ADRs)](docs/adr/).
---

## 🗄️ Modelo de Dados (Decisões de Design)

### Chaves Primárias (UUID vs Long)
* **UUIDv4** é utilizado para `User`, `Event`, `Registration` e `Address` para mitigar problemas de enumeração de recursos expostos na URL.
* **Long (Sequencial)** é restrito à tabela `Category` devido ao baixo volume e natureza estática dos dados.

### Relacionamentos Críticos
* **`EVENT_ORGANIZER` (Many-to-Many):** Tabela intermediária criada para suportar o requisito de múltiplos organizadores responsáveis por um mesmo evento, permitindo transferência de propriedade ou co-organização.
* **`REGISTRATION` Unique Constraint:** Chave de unicidade composta entre `user_id` + `event_id` + `status=CONFIRMED` aplicada a nível de banco para garantir que um participante não se inscreva duas vezes no mesmo evento.

---

## 🧠 Solução para Alta Concorrência (Desafio Sênior)
Para mitigar **condições de corrida (race conditions)** quando múltiplos usuários disputam as últimas vagas de um evento simultaneamente, a arquitetura avaliou três abordagens de engenharia de software antes de definir a implementação ideal.

### 📊 Matriz de Decisão Arquitetural

#### 1. Lock Otimista (`@Version`)
* **Como funcionaria:** O Hibernate controlaria uma coluna de versão na tabela `Event`. Se duas requisições lessem a versão `1` e tentassem salvar, a primeira venceria e incrementaria para `2`. A segunda falharia lançando uma `ObjectOptimisticLockingFailureException`.
* **Por que foi rejeitado:** Sob picos de acesso massivos (abertura de lotes), essa abordagem geraria milhares de exceções na camada de aplicação, exigindo lógica complexa de retentativas (*retry*) e estressando o servidor desnecessariamente.

#### 2. Lock Pessimista de Escrita (`PESSIMISTIC_WRITE`)
* **Como funcionaria:** A aplicação executaria um `SELECT ... FOR UPDATE` ao buscar o evento, travando a linha física no PostgreSQL. As demais requisições concorrentes ficariam em uma fila estrita aguardando a liberação.
* **Por que foi rejeitado:** Embora seguro contra overbooking, reter conexões físicas do pool (`HikariPool`) por muito tempo eleva drasticamente a latência do sistema. Sob alta carga, isso esgotaria o pool rapidamente, derrubando a API inteira. Além disso, abriria margem para *Deadlocks* caso regras futuras travassem múltiplos recursos em ordens distintas.

#### 3. Atualização Atômica Não-Bloqueante com Fluxo Invertido (Abordagem Escolhida)
* **Como funciona:** Evita-se qualquer tipo de lock preemptivo na aplicação. A validação de capacidade e o incremento ocorrem diretamente na camada de banco de dados através de uma única instrução SQL atômica:
  ```sql
  UPDATE event SET current_count = current_count + 1 WHERE id = :id AND current_count < capacity;
  ```
* **Por que foi escolhida:** O motor do PostgreSQL gerencia o isolamento dessa operação de forma extremamente performática.
* **Fluxo Sênior Anti-Bug:** Para impedir a inserção de registros órfãos ("dados fantasmas") na tabela de inscrições, o fluxo tradicional foi invertido:
    1. Primeiro, executa-se o `UPDATE` condicional acima.
    2. O Spring Data JPA avalia o retorno do banco. Se houver `1` linha afetada (vaga garantida), o fluxo prossegue e realiza o `INSERT` na tabela `REGISTRATION`.
    3. Se retornar `0` linhas afetadas (evento lotado), uma exceção de negócio é lançada imediatamente, interrompendo a transação sem sequer tocar na tabela de inscrições.
* **Consistência de Memória:** O repositório utiliza `@Modifying(clearAutomatically = true)` para limpar o contexto de persistência do Hibernate imediatamente após a query nativa, eliminando qualquer risco de dados defasados em memória.

---

## 🚀 Como Rodar o Ambiente de Desenvolvimento

### Pré-requisitos
* Docker e Docker Compose instalados.
* Java 21 e Maven (se desejar rodar a aplicação localmente fora do container).

### Inicializando o Banco de Dados (PostgreSQL)
Navegue até a raiz do projeto e execute o comando:
```bash
docker compose up -d
```
O Docker irá provisionar o banco de dados e aplicar o volume local de persistência (`pgdata`). O Spring Boot aplicará as migrations do Flyway automaticamente na inicialização.
