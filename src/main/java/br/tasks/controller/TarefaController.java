package br.tasks.controller;

import br.tasks.dto.error.ErrorResponse;
import br.tasks.dto.tarefa.TarefaRequestDTO;
import br.tasks.dto.tarefa.TarefaResponseDTO;
import br.tasks.exception.UsuarioNaoAutorizadoException;
import br.tasks.security.UsuarioPrincipal;
import br.tasks.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD de tarefas do usuário autenticado.
 */
@RestController
@RequestMapping("/api/v1/tarefas")
@Tag(name = "Tarefas", description = "CRUD de tarefas do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class TarefaController {

    private final TarefaService tarefaService;

    /**
     * Construtor com injeção de dependência.
     *
     * @param tarefaServiceParam serviço de tarefas
     */
    public TarefaController(final TarefaService tarefaServiceParam) {
        this.tarefaService = tarefaServiceParam;
    }

    /**
     * Lista as tarefas do usuário autenticado.
     *
     * @param principal usuário autenticado
     * @param concluida filtro opcional
     * @return tarefas encontradas
     */
    @GetMapping
    @Operation(summary = "Listar tarefas",
            description = "Retorna apenas as tarefas do usuário autenticado. "
                    + "O filtro opcional concluida permite listar pendentes ou concluídas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefas listadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TarefaResponseDTO>> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioPrincipal principal,
            @Parameter(description = "Filtro por status de conclusão", example = "false")
            @RequestParam(required = false) final Boolean concluida) {
        return ResponseEntity.ok(tarefaService.listar(usuarioId(principal), concluida));
    }

    /**
     * Busca uma tarefa do usuário autenticado.
     *
     * @param id        identificador
     * @param principal usuário autenticado
     * @return tarefa encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @tarefaService.isOwner(authentication, #id)")
    @Operation(summary = "Buscar tarefa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TarefaResponseDTO> buscarPorId(
            @PathVariable final Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioPrincipal principal) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id, usuarioId(principal)));
    }

    /**
     * Cria uma tarefa para o usuário autenticado.
     *
     * @param request   dados da tarefa
     * @param principal usuário autenticado
     * @return tarefa criada
     */
    @PostMapping
    @Operation(summary = "Criar tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TarefaResponseDTO> criar(
            @RequestBody @Valid final TarefaRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioPrincipal principal) {
        TarefaResponseDTO criada = tarefaService.criar(request, usuarioId(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    /**
     * Atualiza uma tarefa do usuário autenticado.
     *
     * @param id        identificador
     * @param request   novos dados
     * @param principal usuário autenticado
     * @return tarefa atualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @tarefaService.isOwner(authentication, #id)")
    @Operation(summary = "Atualizar tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TarefaResponseDTO> atualizar(
            @PathVariable final Long id,
            @RequestBody @Valid final TarefaRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioPrincipal principal) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request, usuarioId(principal)));
    }

    /**
     * Exclui uma tarefa do usuário autenticado.
     *
     * @param id        identificador
     * @param principal usuário autenticado
     * @return resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @tarefaService.isOwner(authentication, #id)")
    @Operation(summary = "Excluir tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa excluída"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(
            @PathVariable final Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal final UsuarioPrincipal principal) {
        tarefaService.excluir(id, usuarioId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long usuarioId(final UsuarioPrincipal principal) {
        if (principal == null) {
            throw new UsuarioNaoAutorizadoException("Usuário não autenticado");
        }
        return principal.getId();
    }
}
