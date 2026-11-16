package br.tasks.dto.auth;

import br.tasks.dto.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta do login contendo o token JWT.
 */
@Schema(name = "LoginResponse", description = "Token JWT e dados do usuário autenticado")
public record LoginResponse(

        @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Validade do token em segundos", example = "86400")
        long expiresIn,

        @Schema(description = "Dados do usuário autenticado")
        UsuarioResponse usuario
) {
}
