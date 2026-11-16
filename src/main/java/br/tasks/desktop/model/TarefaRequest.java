package br.tasks.desktop.model;

/**
 * Corpo enviado na criação/atualização de tarefas.
 */
public record TarefaRequest(String descricao, Prioridade prioridade, Boolean concluida) {
}
