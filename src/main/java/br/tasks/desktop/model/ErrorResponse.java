package br.tasks.desktop.model;

import java.util.Map;

/**
 * Corpo de erro padronizado retornado pela API.
 */
public record ErrorResponse(String timestamp, int status, String error, String message,
                            String path, Map<String, String> fieldErrors) {
}
