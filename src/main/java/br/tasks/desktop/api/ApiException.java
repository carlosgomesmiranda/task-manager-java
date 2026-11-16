package br.tasks.desktop.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Erro de comunicação ou de negócio retornado pela API.
 */
public final class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final Map<String, String> fieldErrors;

    /**
     * Cria a exceção com status e erros por campo.
     *
     * @param mensagem    mensagem legível
     * @param statusParam código HTTP (0 quando não houve resposta)
     * @param erros       erros de validação por campo
     */
    public ApiException(final String mensagem, final int statusParam,
                        final Map<String, String> erros) {
        super(mensagem);
        this.status = statusParam;
        this.fieldErrors = erros == null ? Map.of() : new LinkedHashMap<>(erros);
    }

    /**
     * Cria a exceção com status e causa.
     *
     * @param mensagem    mensagem legível
     * @param statusParam código HTTP (0 quando não houve resposta)
     * @param causa       causa original
     */
    public ApiException(final String mensagem, final int statusParam, final Throwable causa) {
        super(mensagem, causa);
        this.status = statusParam;
        this.fieldErrors = Map.of();
    }

    /**
     * Cria a exceção com status.
     *
     * @param mensagem    mensagem legível
     * @param statusParam código HTTP
     */
    public ApiException(final String mensagem, final int statusParam) {
        super(mensagem);
        this.status = statusParam;
        this.fieldErrors = Map.of();
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getFieldErrors() {
        return Map.copyOf(fieldErrors);
    }

    /**
     * Monta a mensagem completa, incluindo erros por campo quando existirem.
     *
     * @return mensagem pronta para exibição
     */
    public String getMensagemCompleta() {
        if (fieldErrors.isEmpty()) {
            return getMessage();
        }
        StringBuilder texto = new StringBuilder(getMessage());
        fieldErrors.forEach((campo, erro) ->
                texto.append(System.lineSeparator()).append("• ").append(campo)
                        .append(": ").append(erro));
        return texto.toString();
    }
}
