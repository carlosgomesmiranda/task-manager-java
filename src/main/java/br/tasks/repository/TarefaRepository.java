package br.tasks.repository;

import br.tasks.domain.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório de tarefas. As consultas JPQL carregam o usuário com
 * {@code join fetch} para evitar consultas N+1 na conversão para DTO.
 */
@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    /**
     * Lista todas as tarefas de um usuário.
     *
     * @param usuarioId identificador do usuário
     * @return tarefas do usuário
     */
    @Query("select t from Tarefa t join fetch t.usuario where t.usuario.id = :usuarioId")
    List<Tarefa> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista as tarefas de um usuário filtrando pelo status de conclusão.
     *
     * @param usuarioId identificador do usuário
     * @param concluida status desejado
     * @return tarefas do usuário que correspondem ao filtro
     */
    @Query("select t from Tarefa t join fetch t.usuario "
            + "where t.usuario.id = :usuarioId and t.concluida = :concluida")
    List<Tarefa> findByUsuarioIdAndConcluida(@Param("usuarioId") Long usuarioId,
                                             @Param("concluida") boolean concluida);
}
