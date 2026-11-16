package br.tasks.exception;

/**
 * Lançada quando um usuário tenta acessar, alterar ou excluir um recurso
 * que pertence a outro usuário.
 */
public class UsuarioNaoAutorizadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param mensagem detalhe da violação de acesso
     */
    public UsuarioNaoAutorizadoException(final String mensagem) {
        super(mensagem);
    }
}
