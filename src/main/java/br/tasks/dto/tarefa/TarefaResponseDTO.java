package br.tasks.dto.tarefa;

import br.tasks.domain.Prioridade;
import br.tasks.domain.Tarefa;
import br.tasks.dto.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representação de uma tarefa retornada pela API.
 */
@Schema(name = "TarefaResponseDTO", description = "Tarefa e o usuário proprietário")
public record TarefaResponseDTO(

        @Schema(description = "Identificador da tarefa", example = "10")
        Long id,

        @Schema(description = "Descrição da tarefa", example = "Estudar Spring Security")
        String descricao,

        @Schema(description = "Prioridade da tarefa", example = "ALTA")
        Prioridade prioridade,

        @Schema(description = "Indica se a tarefa está concluída", example = "false")
        boolean concluida,

        @Schema(description = "Usuário proprietário da tarefa")
        UsuarioResponse usuario
) {

    /**
     * Converte a entidade para o DTO de resposta.
     *
     * @param tarefa entidade de origem
     * @return DTO da tarefa
     */
    public static TarefaResponseDTO from(final Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getDescricao(),
                tarefa.getPrioridade(),
                tarefa.isConcluida(),
                UsuarioResponse.from(tarefa.getUsuario())
        );
    }
}
