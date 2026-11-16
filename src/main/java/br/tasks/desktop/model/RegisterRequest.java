package br.tasks.desktop.model;

/**
 * Dados enviados ao endpoint de cadastro.
 */
public record RegisterRequest(String nome, String email, String senha) {
}
