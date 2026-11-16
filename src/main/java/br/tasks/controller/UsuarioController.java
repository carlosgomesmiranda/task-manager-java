package br.tasks.controller;

import br.tasks.dto.auth.LoginRequest;
import br.tasks.dto.auth.LoginResponse;
import br.tasks.dto.auth.RegisterRequest;
import br.tasks.dto.error.ErrorResponse;
import br.tasks.dto.usuario.UsuarioResponse;
import br.tasks.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de cadastro e autenticação de usuários.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro e login de usuários")
public class UsuarioController {

    private final AuthService authService;

    /**
     * Construtor com injeção de dependência.
     *
     * @param authServiceParam serviço de autenticação
     */
    public UsuarioController(final AuthService authServiceParam) {
        this.authService = authServiceParam;
    }

    /**
     * Cadastra um novo usuário.
     *
     * @param request dados do cadastro
     * @return usuário criado
     */
    @PostMapping("/register")
    @Operation(summary = "Cadastrar usuário",
            description = "Cria um usuário com senha codificada em BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> registrar(
            @RequestBody @Valid final RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    /**
     * Autentica o usuário e gera um token JWT.
     *
     * @param request credenciais
     * @return token JWT e dados do usuário
     */
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário",
            description = "Valida as credenciais e retorna um token JWT no padrão Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid final LoginRequest request) {
        return ResponseEntity.ok(authService.autenticar(request));
    }
}
