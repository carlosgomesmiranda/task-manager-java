package br.tasks.domain;

/**
 * Níveis de prioridade possíveis para uma tarefa.
 *
 * <p>A prioridade é persistida como texto ({@code BAIXA}, {@code MEDIA} ou
 * {@code ALTA}) para manter os dados legíveis no banco de dados.</p>
 */
public enum Prioridade {
    BAIXA,
    MEDIA,
    ALTA
}
