package br.tasks.desktop.api;

import br.tasks.desktop.model.ErrorResponse;
import br.tasks.desktop.model.LoginRequest;
import br.tasks.desktop.model.LoginResponse;
import br.tasks.desktop.model.RegisterRequest;
import br.tasks.desktop.model.TarefaRequest;
import br.tasks.desktop.model.TarefaResponse;
import br.tasks.desktop.model.UsuarioResponse;
import br.tasks.desktop.session.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP da API TaskManager.
 *
 * <p>Substitui qualquer acesso JDBC direto: todas as operações passam pela API
 * REST e, quando necessário, enviam o token JWT no cabeçalho
 * {@code Authorization: Bearer}.</p>
 */
public final class ApiClient {

    private static final String URL_PADRAO = "http://localhost:8080";
    private static final String JSON = "application/json";
    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(20);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final Session session;

    /**
     * Cria o cliente usando a URL configurada em {@code -Dtaskmanager.api.url},
     * na variável de ambiente {@code TASKMGR_API_URL} ou o padrão
     * {@code http://localhost:8080}.
     *
     * @param sessionParam sessão compartilhada da aplicação
     */
    public ApiClient(final Session sessionParam) {
        this(sessionParam, resolverBaseUrl());
    }

    /**
     * Cria o cliente com uma URL explícita.
     *
     * @param sessionParam sessão compartilhada
     * @param baseUrlParam URL base da API (sem barra final)
     */
    public ApiClient(final Session sessionParam, final String baseUrlParam) {
        this.session = sessionParam;
        this.baseUrl = baseUrlParam.endsWith("/")
                ? baseUrlParam.substring(0, baseUrlParam.length() - 1)
                : baseUrlParam;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * POST /api/auth/register.
     *
     * @param request dados do cadastro
     * @return usuário criado
     */
    public UsuarioResponse registrar(final RegisterRequest request) {
        HttpResponse<String> resposta = executar("POST", "/api/auth/register", request, false);
        return ler(resposta.body(), UsuarioResponse.class);
    }

    /**
     * POST /api/auth/login. Armazena o token recebido na sessão em memória.
     *
     * @param request credenciais
     * @return dados do login
     */
    public LoginResponse login(final LoginRequest request) {
        HttpResponse<String> resposta = executar("POST", "/api/auth/login", request, false);
        LoginResponse login = ler(resposta.body(), LoginResponse.class);
        session.iniciar(login.token(), login.usuario());
        return login;
    }

    /**
     * GET /api/v1/tarefas.
     *
     * @param concluida filtro opcional
     * @return tarefas do usuário autenticado
     */
    public List<TarefaResponse> listar(final Boolean concluida) {
        String caminho = "/api/v1/tarefas";
        if (concluida != null) {
            caminho = caminho + "?concluida=" + concluida;
        }
        HttpResponse<String> resposta = executar("GET", caminho, null, true);
        return lerLista(resposta.body());
    }

    /**
     * GET /api/v1/tarefas/{id}.
     *
     * @param id identificador
     * @return tarefa encontrada
     */
    public TarefaResponse buscar(final Long id) {
        HttpResponse<String> resposta = executar("GET", "/api/v1/tarefas/" + id, null, true);
        return ler(resposta.body(), TarefaResponse.class);
    }

    /**
     * POST /api/v1/tarefas.
     *
     * @param request dados da tarefa
     * @return tarefa criada
     */
    public TarefaResponse criar(final TarefaRequest request) {
        HttpResponse<String> resposta = executar("POST", "/api/v1/tarefas", request, true);
        return ler(resposta.body(), TarefaResponse.class);
    }

    /**
     * PUT /api/v1/tarefas/{id}.
     *
     * @param id      identificador
     * @param request novos dados
     * @return tarefa atualizada
     */
    public TarefaResponse atualizar(final Long id, final TarefaRequest request) {
        HttpResponse<String> resposta = executar("PUT", "/api/v1/tarefas/" + id, request, true);
        return ler(resposta.body(), TarefaResponse.class);
    }

    /**
     * DELETE /api/v1/tarefas/{id}.
     *
     * @param id identificador
     */
    public void excluir(final Long id) {
        executar("DELETE", "/api/v1/tarefas/" + id, null, true);
    }

    private HttpResponse<String> executar(final String metodo, final String caminho,
                                           final Object corpo, final boolean autenticado) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + caminho))
                .timeout(TEMPO_LIMITE)
                .header("Accept", JSON);

        if (corpo != null) {
            builder.header("Content-Type", JSON);
        }

        if (autenticado) {
            builder.header("Authorization", "Bearer " + exigirToken());
        }

        HttpRequest.BodyPublisher publisher = corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(serializar(corpo), StandardCharsets.UTF_8);

        builder.method(metodo, publisher);

        try {
            HttpResponse<String> resposta = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                throw criarErro(resposta);
            }

            return resposta;

        } catch (IOException ex) {
            throw new ApiException(
                    "Não foi possível conectar à API em " + baseUrl
                            + ". Verifique se o backend está em execução.",
                    0, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException("Requisição interrompida.", 0, ex);
        }
    }

    private String exigirToken() {
        String token = session.getToken();
        if (token == null || token.isBlank()) {
            throw new ApiException("Sessão expirada. Faça login novamente.", 401);
        }
        return token;
    }

    private String serializar(final Object corpo) {
        try {
            return objectMapper.writeValueAsString(corpo);
        } catch (JsonProcessingException ex) {
            throw new ApiException("Não foi possível serializar os dados da requisição.", 0, ex);
        }
    }

    private <T> T ler(final String json, final Class<T> tipo) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, tipo);
        } catch (JsonProcessingException ex) {
            throw new ApiException("Resposta inesperada da API.", 0, ex);
        }
    }

    private List<TarefaResponse> lerLista(final String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<TarefaResponse>>() { });
        } catch (JsonProcessingException ex) {
            throw new ApiException("Resposta inesperada da API.", 0, ex);
        }
    }

    private ApiException criarErro(final HttpResponse<String> resposta) {
        int status = resposta.statusCode();
        String corpo = resposta.body();

        if (status == 401) {
            session.encerrar();
        }

        if (corpo != null && !corpo.isBlank()) {
            try {
                ErrorResponse erro = objectMapper.readValue(corpo, ErrorResponse.class);
                String mensagem = erro.message() == null || erro.message().isBlank()
                        ? "Erro HTTP " + status
                        : erro.message();
                return new ApiException(mensagem, status, erro.fieldErrors());
            } catch (JsonProcessingException ignored) {
                // Corpo não JSON: usa o fallback abaixo.
            }
        }

        return new ApiException("Erro HTTP " + status + " ao acessar a API.", status);
    }

    /**
     * URL base utilizada pelo cliente.
     *
     * @return URL base da API
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    private static String resolverBaseUrl() {
        String propriedade = System.getProperty("taskmanager.api.url");
        if (propriedade != null && !propriedade.isBlank()) {
            return propriedade.trim();
        }

        String ambiente = System.getenv("TASKMGR_API_URL");
        if (ambiente != null && !ambiente.isBlank()) {
            return ambiente.trim();
        }

        return URL_PADRAO;
    }
}
