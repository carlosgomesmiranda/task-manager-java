package br.tasks.desktop.model;

/**
 * Tarefa retornada pela API.
 */
public record TarefaResponse(Long id, String descricao, Prioridade prioridade,
                             boolean concluida, UsuarioResponse usuario) {
}
