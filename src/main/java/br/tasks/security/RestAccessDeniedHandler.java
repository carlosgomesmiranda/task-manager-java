package br.tasks.security;

import br.tasks.dto.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Retorna erros 403 em JSON quando o usuário autenticado não possui permissão.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Construtor com injeção de dependência.
     *
     * @param objectMapper serializador JSON do Spring Boot
     */
    public RestAccessDeniedHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
                       final AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponse erro = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Você não possui permissão para acessar este recurso.",
                request.getRequestURI(),
                null
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), erro);
    }
}
