package br.tasks.exception;

import br.tasks.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de exceções da API. Todas as respostas de erro seguem o
 * mesmo formato {@link ErrorResponse}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Recurso inexistente.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 404
     */
    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> tratarTarefaNaoEncontrada(
            final TarefaNaoEncontradaException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * Usuário autenticado inexistente.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 404
     */
    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> tratarUsuarioNaoEncontrado(
            final UsuarioNaoEncontradoException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * Acesso a recurso de outro usuário.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 403
     */
    @ExceptionHandler(UsuarioNaoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> tratarAcessoNaoAutorizado(
            final UsuarioNaoAutorizadoException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    /**
     * Validação de regra de negócio.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 400
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> tratarValidacaoNegocio(
            final ValidationException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /**
     * Erros de Bean Validation no corpo da requisição.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 400 com erros por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarValidacaoCorpo(
            final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(erro -> campos.putIfAbsent("requisicao", erro.getDefaultMessage()));

        return resposta(HttpStatus.BAD_REQUEST,
                "Um ou mais campos da requisição são inválidos", request, campos);
    }

    /**
     * Erros de validação em parâmetros de rota/query.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 400
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> tratarConstraintViolation(
            final ConstraintViolationException ex, final HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violacao ->
                campos.put(violacao.getPropertyPath().toString(), violacao.getMessage()));

        return resposta(HttpStatus.BAD_REQUEST,
                "Um ou mais parâmetros são inválidos", request, campos);
    }

    /**
     * Credenciais inválidas.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> tratarCredenciaisInvalidas(
            final BadCredentialsException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos", request, null);
    }

    /**
     * Falhas de autenticação em geral.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> tratarAutenticacao(
            final AuthenticationException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.UNAUTHORIZED,
                "Falha na autenticação: " + ex.getMessage(), request, null);
    }

    /**
     * Acesso negado por regra de autorização.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> tratarAcessoNegado(
            final AccessDeniedException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.FORBIDDEN,
                "Você não possui permissão para acessar este recurso", request, null);
    }

    /**
     * Corpo JSON ausente ou malformado.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> tratarCorpoInvalido(
            final HttpMessageNotReadableException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou malformado. "
                        + "Verifique os tipos dos campos (prioridade aceita BAIXA, MEDIA ou ALTA).",
                request, null);
    }

    /**
     * Tipo inválido em parâmetro de rota ou query.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tratarTipoInvalido(
            final MethodArgumentTypeMismatchException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'", request, null);
    }

    /**
     * Método HTTP não suportado pelo endpoint.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tratarMetodoNaoSuportado(
            final HttpRequestMethodNotSupportedException ex, final HttpServletRequest request) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não suportado para este endpoint", request, null);
    }

    /**
     * Violação de integridade do banco (por exemplo, e-mail duplicado em corrida).
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> tratarIntegridade(
            final DataIntegrityViolationException ex, final HttpServletRequest request) {
        LOGGER.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT,
                "Não foi possível concluir a operação: os dados violam uma restrição de integridade.",
                request, null);
    }

    /**
     * Fallback para exceções não tratadas.
     *
     * @param ex      exceção
     * @param request requisição
     * @return resposta 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> tratarErroInesperado(
            final RuntimeException ex, final HttpServletRequest request) {
        LOGGER.error("Erro não tratado na API", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado. Tente novamente mais tarde.", request, null);
    }

    private ResponseEntity<ErrorResponse> resposta(final HttpStatus status, final String mensagem,
                                                   final HttpServletRequest request,
                                                   final Map<String, String> fieldErrors) {
        ErrorResponse erro = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors
        );
        return ResponseEntity.status(status).body(erro);
    }
}
