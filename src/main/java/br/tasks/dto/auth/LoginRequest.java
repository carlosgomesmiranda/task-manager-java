package br.tasks.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciais de autenticação.
 */
@Schema(name = "LoginRequest", description = "Credenciais para autenticação")
public record LoginRequest(

        @Schema(description = "E-mail cadastrado", example = "maria@example.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        String email,

        @Schema(description = "Senha do usuário", example = "senha123")
        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
