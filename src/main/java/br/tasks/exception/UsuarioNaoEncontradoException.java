package br.tasks.exception;

/**
 * Lançada quando o usuário autenticado não é encontrado na base de dados.
 */
public class UsuarioNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param mensagem detalhe do usuário não encontrado
     */
    public UsuarioNaoEncontradoException(final String mensagem) {
        super(mensagem);
    }
}
