package br.tasks.security;

import br.tasks.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Implementação de {@link UserDetailsService} utilizada pelo
 * {@code DaoAuthenticationProvider} e pelo filtro JWT.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Construtor com injeção de dependência.
     *
     * @param usuarioRepository repositório de usuários
     */
    public CustomUserDetailsService(final UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) {
        String email = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return usuarioRepository.findByEmail(email)
                .map(UsuarioPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com e-mail " + email));
    }
}
