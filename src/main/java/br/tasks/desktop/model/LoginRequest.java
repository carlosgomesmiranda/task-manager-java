package br.tasks.desktop.model;

/**
 * Credenciais enviadas ao endpoint de login.
 */
public record LoginRequest(String email, String senha) {
}
