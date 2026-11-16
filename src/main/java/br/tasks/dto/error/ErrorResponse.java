package br.tasks.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Corpo padronizado de erro retornado pela API.
 */
@Schema(name = "ErrorResponse", description = "Detalhes padronizados de um erro")
public record ErrorResponse(

        @Schema(description = "Momento do erro", example = "2026-01-01T12:00:00Z")
        Instant timestamp,

        @Schema(description = "Código HTTP", example = "404")
        int status,

        @Schema(description = "Resumo do erro", example = "Not Found")
        String error,

        @Schema(description = "Mensagem detalhada", example = "Tarefa não encontrada com id 10")
        String message,

        @Schema(description = "Caminho da requisição", example = "/api/v1/tarefas/10")
        String path,

        @Schema(description = "Erros de validação por campo")
        Map<String, String> fieldErrors
) {
}
