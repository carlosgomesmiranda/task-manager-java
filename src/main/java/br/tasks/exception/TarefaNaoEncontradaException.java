package br.tasks.exception;

/**
 * Lançada quando uma tarefa solicitada não existe.
 */
public class TarefaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param mensagem detalhe da tarefa não encontrada
     */
    public TarefaNaoEncontradaException(final String mensagem) {
        super(mensagem);
    }
}
