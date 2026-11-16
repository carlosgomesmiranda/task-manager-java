package br.tasks.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para o cadastro de um usuário.
 */
@Schema(name = "RegisterRequest", description = "Dados para cadastro de um novo usuário")
public record RegisterRequest(

        @Schema(description = "Nome completo", example = "Maria Silva")
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String nome,

        @Schema(description = "E-mail do usuário", example = "maria@example.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres")
        String email,

        @Schema(description = "Senha com 6 a 72 caracteres", example = "senha123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String senha
) {
}
