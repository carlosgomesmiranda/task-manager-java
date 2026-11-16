package br.tasks.service.impl;

import br.tasks.domain.Tarefa;
import br.tasks.domain.Usuario;
import br.tasks.dto.tarefa.TarefaRequestDTO;
import br.tasks.dto.tarefa.TarefaResponseDTO;
import br.tasks.exception.TarefaNaoEncontradaException;
import br.tasks.exception.UsuarioNaoAutorizadoException;
import br.tasks.exception.UsuarioNaoEncontradoException;
import br.tasks.repository.TarefaRepository;
import br.tasks.repository.UsuarioRepository;
import br.tasks.security.UsuarioPrincipal;
import br.tasks.service.TarefaService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação das regras de negócio de tarefas. A conversão entre DTOs e
 * entidades é realizada pelo {@link ModelMapper}.
 */
@Service("tarefaService")
@Transactional(readOnly = true)
public class TarefaServiceImpl implements TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    /**
     * Construtor com injeção de dependência.
     *
     * @param tarefaRepositoryParam   repositório de tarefas
     * @param usuarioRepositoryParam  repositório de usuários
     * @param modelMapperParam        mapper DTO ↔ entidade
     */
    public TarefaServiceImpl(final TarefaRepository tarefaRepositoryParam,
                             final UsuarioRepository usuarioRepositoryParam,
                             final ModelMapper modelMapperParam) {
        this.tarefaRepository = tarefaRepositoryParam;
        this.usuarioRepository = usuarioRepositoryParam;
        this.modelMapper = modelMapperParam;
    }

    @Override
    @Transactional
    public TarefaResponseDTO criar(final TarefaRequestDTO request, final Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                        "Usuário autenticado não encontrado com id " + usuarioId));

        Tarefa tarefa = modelMapper.map(request, Tarefa.class);
        tarefa.setUsuario(usuario);

        return modelMapper.map(tarefaRepository.save(tarefa), TarefaResponseDTO.class);
    }

    @Override
    public List<TarefaResponseDTO> listar(final Long usuarioId, final Boolean concluida) {
        List<Tarefa> tarefas = concluida == null
                ? tarefaRepository.findByUsuarioId(usuarioId)
                : tarefaRepository.findByUsuarioIdAndConcluida(usuarioId, concluida);

        return tarefas.stream()
                .map(tarefa -> modelMapper.map(tarefa, TarefaResponseDTO.class))
                .toList();
    }

    @Override
    public TarefaResponseDTO buscarPorId(final Long id, final Long usuarioId) {
        return modelMapper.map(buscarDoUsuario(id, usuarioId), TarefaResponseDTO.class);
    }

    @Override
    @Transactional
    public TarefaResponseDTO atualizar(final Long id, final TarefaRequestDTO request,
                                       final Long usuarioId) {
        Tarefa tarefa = buscarDoUsuario(id, usuarioId);
        Tarefa dados = modelMapper.map(request, Tarefa.class);

        tarefa.setDescricao(dados.getDescricao());
        tarefa.setPrioridade(dados.getPrioridade());
        if (request.concluida() != null) {
            tarefa.setConcluida(request.concluida());
        }

        return modelMapper.map(tarefaRepository.save(tarefa), TarefaResponseDTO.class);
    }

    @Override
    @Transactional
    public void excluir(final Long id, final Long usuarioId) {
        Tarefa tarefa = buscarDoUsuario(id, usuarioId);
        tarefaRepository.delete(tarefa);
    }

    @Override
    public boolean isOwner(final Authentication authentication, final Long tarefaId) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return false;
        }

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(
                        "Tarefa não encontrada com id " + tarefaId));

        return tarefa.getUsuario().getId().equals(principal.getId());
    }

    /**
     * Carrega a tarefa e valida a propriedade. Uma tarefa inexistente gera 404;
     * uma tarefa de outro usuário gera 403.
     */
    private Tarefa buscarDoUsuario(final Long id, final Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(
                        "Tarefa não encontrada com id " + id));

        if (!tarefa.getUsuario().getId().equals(usuarioId)) {
            throw new UsuarioNaoAutorizadoException(
                    "Você não tem permissão para acessar a tarefa de id " + id);
        }

        return tarefa;
    }
}
