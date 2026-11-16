package br.tasks.service.impl;

import br.tasks.domain.Usuario;
import br.tasks.dto.auth.LoginRequest;
import br.tasks.dto.auth.LoginResponse;
import br.tasks.dto.auth.RegisterRequest;
import br.tasks.dto.usuario.UsuarioResponse;
import br.tasks.exception.ValidationException;
import br.tasks.repository.UsuarioRepository;
import br.tasks.security.JwtService;
import br.tasks.security.UsuarioPrincipal;
import br.tasks.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Implementação das regras de cadastro e login.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Construtor com injeção de dependência.
     *
     * @param usuarioRepositoryParam    repositório de usuários
     * @param passwordEncoderParam      codificador BCrypt
     * @param authenticationManagerParam gerenciador de autenticação
     * @param jwtServiceParam           serviço de JWT
     */
    public AuthServiceImpl(final UsuarioRepository usuarioRepositoryParam,
                           final PasswordEncoder passwordEncoderParam,
                           final AuthenticationManager authenticationManagerParam,
                           final JwtService jwtServiceParam) {
        this.usuarioRepository = usuarioRepositoryParam;
        this.passwordEncoder = passwordEncoderParam;
        this.authenticationManager = authenticationManagerParam;
        this.jwtService = jwtServiceParam;
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(final RegisterRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new ValidationException("Já existe um usuário cadastrado com o e-mail " + email);
        }

        Usuario usuario = new Usuario(
                request.nome().trim(),
                email,
                passwordEncoder.encode(request.senha())
        );

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse autenticar(final LoginRequest request) {
        String email = normalizarEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.senha())
        );

        if (!(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AuthenticationServiceException("Não foi possível autenticar o usuário");
        }

        String token = jwtService.gerarToken(principal);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoEmSegundos(),
                new UsuarioResponse(principal.getId(), principal.getNome(), principal.getUsername())
        );
    }

    private String normalizarEmail(final String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
