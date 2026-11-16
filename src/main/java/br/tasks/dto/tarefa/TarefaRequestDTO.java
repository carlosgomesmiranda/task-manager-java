package br.tasks.dto.tarefa;

import br.tasks.domain.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para criação e atualização de tarefas.
 */
@Schema(name = "TarefaRequestDTO", description = "Dados para criar ou atualizar uma tarefa")
public record TarefaRequestDTO(

        @Schema(description = "Descrição da tarefa", example = "Estudar Spring Security")
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @Schema(description = "Prioridade da tarefa", example = "ALTA")
        @NotNull(message = "A prioridade é obrigatória")
        Prioridade prioridade,

        @Schema(description = "Indica se a tarefa está concluída", example = "false")
        Boolean concluida
) {
}
