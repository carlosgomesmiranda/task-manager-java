package br.tasks.service;

import br.tasks.config.ModelMapperConfig;
import br.tasks.domain.Prioridade;
import br.tasks.domain.Tarefa;
import br.tasks.domain.Usuario;
import br.tasks.dto.tarefa.TarefaRequestDTO;
import br.tasks.dto.tarefa.TarefaResponseDTO;
import br.tasks.exception.TarefaNaoEncontradaException;
import br.tasks.exception.UsuarioNaoAutorizadoException;
import br.tasks.repository.TarefaRepository;
import br.tasks.repository.UsuarioRepository;
import br.tasks.security.UsuarioPrincipal;
import br.tasks.service.impl.TarefaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceImplTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private TarefaServiceImpl tarefaService;

    private Usuario maria;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
        tarefaService = new TarefaServiceImpl(tarefaRepository, usuarioRepository, modelMapper);
        maria = new Usuario("Maria", "maria@example.com", "hash");
        ReflectionTestUtils.setField(maria, "id", 1L);
    }

    @Test
    void deveCriarTarefaParaUsuarioAutenticado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocacao -> {
            Tarefa tarefa = invocacao.getArgument(0);
            ReflectionTestUtils.setField(tarefa, "id", 10L);
            return tarefa;
        });

        TarefaResponseDTO resposta = tarefaService.criar(
                new TarefaRequestDTO("Estudar Spring Security", Prioridade.ALTA, false), 1L);

        assertEquals(10L, resposta.id());
        assertEquals("Estudar Spring Security", resposta.descricao());
        assertEquals(Prioridade.ALTA, resposta.prioridade());
        assertFalse(resposta.concluida());
        assertEquals(1L, resposta.usuario().id());
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    void deveListarTodasAsTarefasDoUsuario() {
        Tarefa pendente = criarTarefa(10L, "Pendente", maria, false);
        Tarefa concluida = criarTarefa(11L, "Concluída", maria, true);
        when(tarefaRepository.findByUsuarioId(1L)).thenReturn(List.of(pendente, concluida));

        List<TarefaResponseDTO> resposta = tarefaService.listar(1L, null);

        assertEquals(2, resposta.size());
        assertEquals("Pendente", resposta.get(0).descricao());
        assertEquals("Concluída", resposta.get(1).descricao());
        verify(tarefaRepository).findByUsuarioId(1L);
        verify(tarefaRepository, never()).findByUsuarioIdAndConcluida(any(), anyBoolean());
    }

    @Test
    void deveListarSomenteTarefasConcluidasQuandoFiltrado() {
        Tarefa concluida = criarTarefa(11L, "Concluída", maria, true);
        when(tarefaRepository.findByUsuarioIdAndConcluida(1L, true))
                .thenReturn(List.of(concluida));

        List<TarefaResponseDTO> resposta = tarefaService.listar(1L, true);

        assertEquals(1, resposta.size());
        assertTrue(resposta.get(0).concluida());
        verify(tarefaRepository).findByUsuarioIdAndConcluida(1L, true);
    }

    @Test
    void deveBuscarTarefaDoUsuario() {
        Tarefa tarefa = criarTarefa(10L, "Minha tarefa", maria, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        TarefaResponseDTO resposta = tarefaService.buscarPorId(10L, 1L);

        assertEquals(10L, resposta.id());
        assertEquals("Minha tarefa", resposta.descricao());
    }

    @Test
    void deveLancarNotFoundQuandoTarefaNaoExiste() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TarefaNaoEncontradaException.class,
                () -> tarefaService.buscarPorId(99L, 1L));
    }

    @Test
    void naoDevePermitirBuscarTarefaDeOutroUsuario() {
        Usuario bruno = new Usuario("Bruno", "bruno@example.com", "hash");
        ReflectionTestUtils.setField(bruno, "id", 2L);
        Tarefa tarefa = criarTarefa(10L, "Tarefa do Bruno", bruno, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        assertThrows(UsuarioNaoAutorizadoException.class,
                () -> tarefaService.buscarPorId(10L, 1L));
    }

    @Test
    void naoDevePermitirAtualizarTarefaDeOutroUsuario() {
        Usuario bruno = new Usuario("Bruno", "bruno@example.com", "hash");
        ReflectionTestUtils.setField(bruno, "id", 2L);
        Tarefa tarefa = criarTarefa(10L, "Tarefa do Bruno", bruno, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        TarefaRequestDTO request = new TarefaRequestDTO("Alterada", Prioridade.BAIXA, true);

        assertThrows(UsuarioNaoAutorizadoException.class,
                () -> tarefaService.atualizar(10L, request, 1L));
        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    void naoDevePermitirExcluirTarefaDeOutroUsuario() {
        Usuario bruno = new Usuario("Bruno", "bruno@example.com", "hash");
        ReflectionTestUtils.setField(bruno, "id", 2L);
        Tarefa tarefa = criarTarefa(10L, "Tarefa do Bruno", bruno, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        assertThrows(UsuarioNaoAutorizadoException.class,
                () -> tarefaService.excluir(10L, 1L));
        verify(tarefaRepository, never()).delete(any(Tarefa.class));
    }

    @Test
    void deveAtualizarTarefaDoUsuario() {
        Tarefa tarefa = criarTarefa(10L, "Antiga", maria, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(tarefa)).thenReturn(tarefa);

        TarefaResponseDTO resposta = tarefaService.atualizar(10L,
                new TarefaRequestDTO("Nova descrição", Prioridade.BAIXA, true), 1L);

        assertEquals("Nova descrição", resposta.descricao());
        assertEquals(Prioridade.BAIXA, resposta.prioridade());
        assertTrue(resposta.concluida());
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    void deveExcluirTarefaDoUsuario() {
        Tarefa tarefa = criarTarefa(10L, "Excluir", maria, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        tarefaService.excluir(10L, 1L);

        verify(tarefaRepository).delete(tarefa);
    }

    @Test
    void deveConfirmarPosseDaTarefaParaODono() {
        Tarefa tarefa = criarTarefa(10L, "Minha tarefa", maria, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        UsuarioPrincipal principal = new UsuarioPrincipal(maria);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        assertTrue(tarefaService.isOwner(authentication, 10L));
    }

    @Test
    void deveNegarPosseDaTarefaParaOutroUsuario() {
        Usuario bruno = new Usuario("Bruno", "bruno@example.com", "hash");
        ReflectionTestUtils.setField(bruno, "id", 2L);
        Tarefa tarefa = criarTarefa(10L, "Tarefa do Bruno", bruno, false);
        when(tarefaRepository.findById(10L)).thenReturn(Optional.of(tarefa));

        UsuarioPrincipal principal = new UsuarioPrincipal(maria);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        assertFalse(tarefaService.isOwner(authentication, 10L));
    }

    @Test
    void deveLancarNotFoundQuandoTarefaNaoExisteNaVerificacaoDePosse() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        UsuarioPrincipal principal = new UsuarioPrincipal(maria);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        assertThrows(TarefaNaoEncontradaException.class,
                () -> tarefaService.isOwner(authentication, 99L));
    }

    private Tarefa criarTarefa(final Long id, final String descricao,
                               final Usuario usuario, final boolean concluida) {
        Tarefa tarefa = new Tarefa(descricao, Prioridade.MEDIA, concluida, usuario);
        ReflectionTestUtils.setField(tarefa, "id", id);
        return tarefa;
    }
}
