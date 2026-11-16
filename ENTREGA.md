# TaskManager — Guia de Teste para o Professor

Pacote de entrega do **TaskManager**, com API REST em Spring Boot 3, segurança JWT,
persistência em MySQL e cliente desktop JavaFX.

## 1. Requisitos

- **JDK 17** ou superior
- **Maven 3.9+** (o NetBeans já inclui Maven)
- **MySQL 8** em execução em `localhost:3306`
- **NetBeans** (opcional, para abrir o projeto)

## 2. Preparar o MySQL

Na raiz do projeto, execute:

```bash
mysql -u root -p < database/setup.sql
```

O script cria:

- banco `taskmanager_db` com `utf8mb4`;
- usuário `taskuser` com senha `taskpass`;
- permissões totais no banco.

As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`).

Se preferir, execute o conteúdo de `database/setup.sql` pelo MySQL Workbench.

## 3. Executar o backend

Na raiz do projeto:

```bash
mvn spring-boot:run
```

Ou no NetBeans:

1. `File → Open Project` e selecione a pasta do projeto.
2. Execute a classe `br.tasks.TaskApplication`.
3. Aguarde a mensagem de aplicação iniciada.

A API ficará disponível em `http://localhost:8080`.

## 4. Acessar o Swagger

Abra no navegador:

- <http://localhost:8080/> — redireciona para o Swagger UI
- <http://localhost:8080/swagger-ui/index.html>

Fluxo sugerido no Swagger:

1. `POST /api/auth/register` — cadastre um usuário.
2. `POST /api/auth/login` — copie o campo `token` da resposta.
3. Clique em **Authorize** e informe o token (sem o prefixo `Bearer`).
4. Teste o CRUD em `/api/v1/tarefas`.

Exemplo de cadastro:

```json
{
  "nome": "Professor",
  "email": "professor@example.com",
  "senha": "senha123"
}
```

Exemplo de tarefa:

```json
{
  "descricao": "Validar o projeto",
  "prioridade": "ALTA",
  "concluida": false
}
```

## 5. Executar o cliente desktop JavaFX

Com o backend ativo:

```bash
mvn javafx:run
```

Ou no NetBeans, execute a classe:

```
br.tasks.desktop.Launcher
```

> Não execute `TaskManagerApp` diretamente no NetBeans; use `Launcher` para evitar o erro
> “JavaFX runtime components are missing”.

## 6. Roteiro de validação

1. Cadastre o **Usuário A** e faça login no cliente JavaFX.
2. Crie, edite, conclua e exclua tarefas.
3. Use os filtros de status e a busca por texto.
4. Cadastre o **Usuário B** em outra sessão.
5. Confirme que o Usuário B **não** consegue visualizar, editar ou excluir tarefas do Usuário A.
6. Confirme os status da API:
   - `201` ao criar tarefa
   - `200` ao listar/atualizar
   - `204` ao excluir
   - `401` sem token
   - `403` ao acessar tarefa de outro usuário
   - `404` para tarefa inexistente
   - `400` para dados inválidos

## 7. Executar os testes automatizados

```bash
mvn clean test
```

Os testes usam **H2 em memória** e não precisam do MySQL. A suíte cobre JWT,
autenticação, regras de tarefas e integração completa da API.

## 8. Gerar o jar executável (opcional)

```bash
mvn clean package
java -jar target/task-0.0.1-SNAPSHOT.jar
```

## 9. Configurações opcionais

As configurações padrão estão em `src/main/resources/application.yml`.
Para sobrescrever, use variáveis de ambiente:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/taskmanager_db...` |
| `DB_USERNAME` | `taskuser` |
| `DB_PASSWORD` | `taskpass` |
| `JWT_SECRET` | chave de desenvolvimento |
| `SERVER_PORT` | `8080` |
| `TASKMGR_API_URL` | `http://localhost:8080` |

## 10. Estrutura do pacote

```
TaskManager/
├── pom.xml
├── README.md
├── ENTREGA.md
├── database/setup.sql
├── img/diagrama.png
└── src/
    ├── main/java/br/tasks/...          # backend Spring Boot
    ├── main/java/br/tasks/desktop/...  # cliente JavaFX
    ├── main/resources/...              # application.yml e CSS
    └── test/...                        # testes unitários e integração
```
