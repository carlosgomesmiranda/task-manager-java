package br.tasks.service;

import br.tasks.domain.Usuario;
import br.tasks.dto.auth.LoginRequest;
import br.tasks.dto.auth.LoginResponse;
import br.tasks.dto.auth.RegisterRequest;
import br.tasks.dto.usuario.UsuarioResponse;
import br.tasks.exception.ValidationException;
import br.tasks.repository.UsuarioRepository;
import br.tasks.security.JwtService;
import br.tasks.security.UsuarioPrincipal;
import br.tasks.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void deveRegistrarUsuarioComEmailNormalizadoESenhaCriptografada() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario usuario = invocacao.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", 1L);
            return usuario;
        });

        UsuarioResponse resposta = authService.registrar(
                new RegisterRequest("  Ana  ", "  ANA@Example.COM ", "senha123"));

        assertEquals(1L, resposta.id());
        assertEquals("Ana", resposta.nome());
        assertEquals("ana@example.com", resposta.email());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("ana@example.com", captor.getValue().getEmail());
        assertEquals("hash-bcrypt", captor.getValue().getSenha());
    }

    @Test
    void naoDeveRegistrarEmailDuplicado() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> authService.registrar(
                        new RegisterRequest("Ana", "ana@example.com", "senha123")));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveAutenticarEGerarTokenJwt() {
        UsuarioPrincipal principal =
                new UsuarioPrincipal(1L, "Ana", "ana@example.com", "hash-bcrypt");
        Authentication autenticacao = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(autenticacao);
        when(jwtService.gerarToken(principal)).thenReturn("jwt-token");
        when(jwtService.getExpiracaoEmSegundos()).thenReturn(3600L);

        LoginResponse resposta = authService.autenticar(
                new LoginRequest(" ANA@EXAMPLE.COM ", "senha123"));

        assertEquals("jwt-token", resposta.token());
        assertEquals("Bearer", resposta.tokenType());
        assertEquals(3600L, resposta.expiresIn());
        assertEquals(1L, resposta.usuario().id());
        assertEquals("ana@example.com", resposta.usuario().email());
    }

    @Test
    void devePropagarCredenciaisInvalidas() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(BadCredentialsException.class,
                () -> authService.autenticar(
                        new LoginRequest("ana@example.com", "senha-errada")));
    }
}
