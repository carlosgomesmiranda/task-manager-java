package br.tasks.service;

import br.tasks.dto.tarefa.TarefaRequestDTO;
import br.tasks.dto.tarefa.TarefaResponseDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Regras de negócio de tarefas. Todas as operações são restritas ao usuário
 * autenticado informado em {@code usuarioId}.
 */
public interface TarefaService {

    /**
     * Cria uma tarefa para o usuário autenticado.
     *
     * @param request   dados da tarefa
     * @param usuarioId identificador do proprietário
     * @return tarefa criada
     */
    TarefaResponseDTO criar(TarefaRequestDTO request, Long usuarioId);

    /**
     * Lista as tarefas do usuário autenticado.
     *
     * @param usuarioId identificador do proprietário
     * @param concluida filtro opcional de conclusão
     * @return tarefas do usuário
     */
    List<TarefaResponseDTO> listar(Long usuarioId, Boolean concluida);

    /**
     * Busca uma tarefa do usuário autenticado.
     *
     * @param id        identificador da tarefa
     * @param usuarioId identificador do proprietário
     * @return tarefa encontrada
     */
    TarefaResponseDTO buscarPorId(Long id, Long usuarioId);

    /**
     * Atualiza uma tarefa do usuário autenticado.
     *
     * @param id        identificador da tarefa
     * @param request   novos dados
     * @param usuarioId identificador do proprietário
     * @return tarefa atualizada
     */
    TarefaResponseDTO atualizar(Long id, TarefaRequestDTO request, Long usuarioId);

    /**
     * Exclui uma tarefa do usuário autenticado.
     *
     * @param id        identificador da tarefa
     * @param usuarioId identificador do proprietário
     */
    void excluir(Long id, Long usuarioId);

    /**
     * Verifica se a tarefa pertence ao usuário autenticado. Utilizado pelo
     * {@code @PreAuthorize} dos endpoints de tarefa.
     *
     * @param authentication autenticação corrente
     * @param tarefaId       identificador da tarefa
     * @return {@code true} se a tarefa pertence ao usuário autenticado
     * @throws br.tasks.exception.TarefaNaoEncontradaException se a tarefa não existir
     */
    boolean isOwner(Authentication authentication, Long tarefaId);
}
