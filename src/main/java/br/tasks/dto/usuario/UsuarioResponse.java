package br.tasks.dto.usuario;

import br.tasks.domain.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representação pública de um usuário. Nunca expõe a senha.
 */
@Schema(name = "UsuarioResponse", description = "Dados públicos do usuário")
public record UsuarioResponse(

        @Schema(description = "Identificador do usuário", example = "1")
        Long id,

        @Schema(description = "Nome completo", example = "Maria Silva")
        String nome,

        @Schema(description = "E-mail", example = "maria@example.com")
        String email
) {

    /**
     * Converte a entidade para o DTO público.
     *
     * @param usuario entidade de origem
     * @return DTO sem dados sensíveis
     */
    public static UsuarioResponse from(final Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
