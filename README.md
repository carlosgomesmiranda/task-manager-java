# TaskManager 📝

**Repositório:** <https://github.com/carlosgomesmiranda/task-manager-java>

Gerenciador de tarefas com **API REST em Spring Boot 3** e **cliente desktop em JavaFX**.
Cada usuário se cadastra, autentica com **JWT** e gerencia apenas as próprias tarefas.

O cliente desktop **não acessa o banco diretamente**: todas as operações passam pela API REST
via `java.net.http.HttpClient`, com o token enviado no cabeçalho `Authorization: Bearer <token>`.

> **Guia rápido de teste:** consulte [`ENTREGA.md`](ENTREGA.md).

---

## Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 17 |
| Build | Maven (wrapper/parent Spring Boot 3.5.6) |
| Backend | Spring Boot Web, Spring Data JPA, Spring Security |
| Mapeamento | ModelMapper 3.2.2 (DTO ↔ entidade) |
| Banco | MySQL 8 (`ddl-auto=update`) |
| Autenticação | JWT (JJWT 0.12.6) + BCrypt |
| Documentação | springdoc-openapi / Swagger UI |
| Cliente desktop | JavaFX 21 + `HttpClient` + Jackson |
| Testes | JUnit 5, Mockito, MockMvc e H2 (perfil `test`) |
| IDE | NetBeans (projeto Maven único) |

---

## Arquitetura

O backend segue a separação em camadas:

```
br.tasks
├── TaskApplication.java        # classe main do Spring Boot
├── config/                     # SecurityConfig, OpenApiConfig, ModelMapperConfig
├── controller/                 # UsuarioController, TarefaController
├── domain/                     # Usuario, Tarefa, Prioridade (entidades JPA)
├── dto/                        # DTOs de request/response + validação
├── exception/                  # exceções de negócio e GlobalExceptionHandler
├── repository/                 # UsuarioRepository, TarefaRepository
├── security/                   # JwtService, JwtAuthenticationFilter, UserDetailsService
└── service/                    # interfaces e implementações (regras de negócio)

br.tasks.desktop                # cliente JavaFX (não depende do backend em tempo de execução)
├── Launcher.java               # entrada usada pelo Maven/NetBeans (evita erro de runtime do JavaFX)
├── TaskManagerApp.java         # Application JavaFX
├── api/ApiClient.java          # cliente HTTP + tratamento de erros da API
├── model/                      # DTOs espelhados do backend
├── session/Session.java        # token JWT em memória
└── ui/                         # LoginView, TarefasView, diálogos e CSS
```

### Modelo de dados

- **Usuario**: `id`, `nome`, `email` (único), `senha` (hash BCrypt).
- **Tarefa**: `id`, `descricao`, `prioridade` (`BAIXA`, `MEDIA`, `ALTA`), `concluida`, `usuario`.
- Relação **1:N** — um usuário possui várias tarefas; a exclusão de um usuário remove suas tarefas (`cascade`).

### Regras de negócio

- Listagem, busca, atualização e exclusão consideram **somente** as tarefas do usuário autenticado.
- A posse é verificada por `@PreAuthorize` (`hasRole('USER') and @tarefaService.isOwner(authentication, #id)`), além da validação defensiva no serviço.
- Tarefa de outro usuário → **403 Forbidden** (`UsuarioNaoAutorizadoException`).
- Tarefa inexistente → **404 Not Found** (`TarefaNaoEncontradaException`).
- Dados inválidos → **400 Bad Request** (`ValidationException` / Bean Validation).
- Requisição sem token ou com token inválido → **401 Unauthorized**.

---

## Pré-requisitos

- **JDK 17** ou superior (o projeto compila com `--release 17`).
- **Maven 3.9+** (ou use o Maven embutido na IDE).
- **MySQL 8** em execução em `localhost:3306`.
- **NetBeans** (recomendado para executar o cliente JavaFX) ou linha de comando.

> O JavaFX 21 é baixado como dependência Maven; não é necessário instalar o SDK do JavaFX separadamente.

---

## Configuração do banco

Execute o script como um usuário administrativo do MySQL:

```bash
mysql -u root -p < database/setup.sql
```

O script cria:

- banco `taskmanager_db` (`utf8mb4`);
- usuário `taskuser` com senha `taskpass`;
- permissões totais no banco.

As tabelas são criadas/atualizadas automaticamente pelo Hibernate (`ddl-auto=update`).

### Variáveis de ambiente (opcionais)

As configurações padrão estão em `src/main/resources/application.yml` e podem ser sobrescritas:

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/taskmanager_db?...` | URL JDBC |
| `DB_USERNAME` | `taskuser` | Usuário do banco |
| `DB_PASSWORD` | `taskpass` | Senha do banco |
| `JWT_SECRET` | chave de desenvolvimento | **Defina uma chave forte em produção** |
| `JWT_EXPIRATION_MS` | `86400000` (24 h) | Validade do token |
| `CORS_ALLOWED_ORIGINS` | `*` | Origens permitidas |
| `SERVER_PORT` | `8080` | Porta da API |
| `TASKMGR_API_URL` | `http://localhost:8080` | URL usada pelo cliente desktop |

No cliente desktop também é possível informar a URL com `-Dtaskmanager.api.url=http://host:porta`.

---

## Como executar

### Backend (API)

Na raiz do projeto:

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

Também é possível gerar o jar executável:

```bash
mvn clean package
java -jar target/task-0.0.1-SNAPSHOT.jar
```

### Cliente desktop (JavaFX)

Com o backend em execução:

```bash
mvn javafx:run
```

O plugin `javafx-maven-plugin` usa `br.tasks.desktop.Launcher` como classe principal.

### No NetBeans

1. **File → Open Project** e selecione a pasta do projeto (projeto Maven).
2. Aguarde o download das dependências.
3. Para a API: execute a classe `br.tasks.TaskApplication`.
4. Para o desktop: execute a classe `br.tasks.desktop.Launcher` (não use `TaskManagerApp` diretamente,
   pois o JavaFX precisa do launcher auxiliar quando executado por classpath).
5. O backend deve estar rodando antes de efetuar login no cliente.

---

## Endpoints da API

Base: `http://localhost:8080`

### Autenticação (públicos)

| Método | Caminho | Descrição | Sucesso |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Cadastra um usuário | `201` |
| `POST` | `/api/auth/login` | Autentica e retorna o token JWT | `200` |

**Registro**

```json
{
  "nome": "Maria Silva",
  "email": "maria@example.com",
  "senha": "senhaSegura123"
}
```

**Login**

```json
{
  "email": "maria@example.com",
  "senha": "senhaSegura123"
}
```

**Resposta do login**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "usuario": {
    "id": 1,
    "nome": "Maria Silva",
    "email": "maria@example.com"
  }
}
```

### Tarefas (requerem `Authorization: Bearer <token>`)

| Método | Caminho | Descrição | Sucesso |
| --- | --- | --- | --- |
| `GET` | `/api/v1/tarefas` | Lista as tarefas do usuário (filtro opcional `?concluida=true/false`) | `200` |
| `GET` | `/api/v1/tarefas/{id}` | Busca uma tarefa | `200` |
| `POST` | `/api/v1/tarefas` | Cria uma tarefa | `201` |
| `PUT` | `/api/v1/tarefas/{id}` | Atualiza uma tarefa | `200` |
| `DELETE` | `/api/v1/tarefas/{id}` | Exclui uma tarefa | `204` |

**Corpo de criação/atualização**

```json
{
  "descricao": "Estudar Spring Security",
  "prioridade": "ALTA",
  "concluida": false
}
```

**Resposta de tarefa**

```json
{
  "id": 10,
  "descricao": "Estudar Spring Security",
  "prioridade": "ALTA",
  "concluida": false,
  "usuario": {
    "id": 1,
    "nome": "Maria Silva",
    "email": "maria@example.com"
  }
}
```

**Formato de erro**

```json
{
  "timestamp": "2026-11-16T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Esta tarefa pertence a outro usuário",
  "path": "/api/v1/tarefas/5",
  "fieldErrors": null
}
```

### Exemplo com `curl`

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@example.com","senha":"senhaSegura123"}'

# Criar tarefa
curl -X POST http://localhost:8080/api/v1/tarefas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -d '{"descricao":"Minha tarefa","prioridade":"MEDIA","concluida":false}'
```

---

## Swagger / OpenAPI

Com a API em execução:

- Raiz da aplicação: <http://localhost:8080/> (redireciona para o Swagger UI)
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Os endpoints de tarefas aparecem com o cadeado `bearerAuth`. Para testar no Swagger:

1. Faça login em `/api/auth/login` e copie o `token`.
2. Clique em **Authorize** e cole o token (sem o prefixo `Bearer`).
3. Execute os endpoints de `/api/v1/tarefas`.

---

## Cliente desktop

Funcionalidades disponíveis na interface JavaFX:

- Tela de **login** e diálogo de **cadastro** de usuário.
- Listagem de tarefas em tabela com **descrição, prioridade, status e usuário**.
- **Criação**, **edição** e **exclusão** de tarefas com confirmação.
- Marcação rápida de tarefa como concluída.
- Filtros por status (todas / pendentes / concluídas) e busca por texto.
- Mensagens de erro amigáveis para falhas de validação, autenticação e conexão.
- **Token mantido apenas em memória** (não é gravado em disco); encerrar o app encerra a sessão.
- Requisições executadas fora da thread de UI, mantendo a interface responsiva.

---

## Testes

```bash
mvn clean test
```

A suíte usa o perfil `test` com **H2 em memória** (`src/test/resources/application-test.yml`), sem depender do MySQL.
A cobertura inclui:

- testes unitários de `JwtService`, `AuthServiceImpl` e `TarefaServiceImpl` (Mockito);
- testes de integração ponta a ponta com MockMvc: registro, login, acesso com JWT,
  CRUD de tarefas, `401` sem token, `403` para tarefa de terceiros, `400` para dados inválidos e `404`.

Para gerar o pacote sem executar os testes:

```bash
mvn -DskipTests package
```

---

## Estrutura de arquivos principal

```
task-manager-main/
├── pom.xml
├── database/setup.sql
├── src/main/resources/application.yml
├── src/main/resources/desktop/desktop.css
├── src/main/java/br/tasks/...          # backend
├── src/main/java/br/tasks/desktop/...  # cliente JavaFX
└── src/test/...                        # testes unitários e de integração
```

---

## Migração realizada

Esta versão conclui a migração do projeto original:

- Spring Boot atualizado para **3.5.6** com namespace `jakarta.*` e **Java 17**.
- Banco migrado de **PostgreSQL** para **MySQL 8**.
- CRUD original (`/tasks/*`) substituído por **API versionada** em `/api/v1/tarefas`,
  com autenticação **JWT**, **BCrypt** e isolamento de dados por usuário.
- DTOs `TarefaRequestDTO` e `TarefaResponseDTO` com Bean Validation e documentação Swagger,
  convertidos por **ModelMapper** (`ModelMapperConfig`).
- Consultas JPQL explícitas com `@Query` em `TarefaRepository` (`findByUsuarioId` e
  `findByUsuarioIdAndConcluida`), com `join fetch` para evitar N+1.
- Controllers retornando **`ResponseEntity`** com os status `201`, `204`, `404` etc.
- Autorização em nível de método com **`@PreAuthorize`** e `TarefaService.isOwner(...)`.
- Tratamento global de erros (`TarefaNaoEncontradaException`, `UsuarioNaoAutorizadoException`,
  `UsuarioNaoEncontradoException`, `ValidationException`, `RuntimeException`).
- Cliente **JavaFX** criado do zero (o projeto original não possuía interface desktop nem acesso JDBC),
  consumindo exclusivamente a API REST.
- Removidos os arquivos obsoletos: `TaskController`, `TaskDTO`, `TaskRepository`, `TaskService`,
  `TaskServiceImpl` e os antigos `application.properties`.

---

## Observações

- Em produção, **defina `JWT_SECRET`** com um valor forte e exclusivo e restrinja `CORS_ALLOWED_ORIGINS`.
- O `ddl-auto=update` é adequado para desenvolvimento; em produção prefira migrações versionadas (Flyway/Liquibase).
- As tabelas legadas `task`/`tasks` podem existir no banco antigo; a aplicação usa `usuarios` e `tarefas`.
