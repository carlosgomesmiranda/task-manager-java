package br.tasks.security;

import br.tasks.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal autenticado mantido no contexto do Spring Security.
 *
 * <p>Expõe o identificador do usuário para que os controllers possam aplicar
 * as regras de propriedade sem consultar novamente o banco de dados.</p>
 */
public final class UsuarioPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String email;
    private final String senha;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Cria o principal a partir da entidade.
     *
     * @param usuario usuário carregado do banco
     */
    public UsuarioPrincipal(final Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getSenha());
    }

    /**
     * Cria o principal com os dados básicos do usuário.
     *
     * @param idParam    identificador
     * @param nomeParam  nome
     * @param emailParam e-mail (username)
     * @param senhaParam hash da senha
     */
    public UsuarioPrincipal(final Long idParam, final String nomeParam,
                            final String emailParam, final String senhaParam) {
        this.id = idParam;
        this.nome = nomeParam;
        this.email = emailParam;
        this.senha = senhaParam;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
