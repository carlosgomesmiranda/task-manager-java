package br.tasks.desktop.model;

/**
 * Resposta do login com o token JWT.
 */
public record LoginResponse(String token, String tokenType, long expiresIn, UsuarioResponse usuario) {
}
