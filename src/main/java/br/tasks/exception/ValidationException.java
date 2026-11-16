package br.tasks.exception;

/**
 * Lançada quando uma regra de negócio de validação é violada.
 *
 * <p>Os erros de Bean Validation continuam sendo tratados separadamente pelo
 * {@link GlobalExceptionHandler}. Esta exceção cobre validações de negócio,
 * como tentativa de cadastrar um e-mail já existente.</p>
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param mensagem detalhe da validação
     */
    public ValidationException(final String mensagem) {
        super(mensagem);
    }
}
