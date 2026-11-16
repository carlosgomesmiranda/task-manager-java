package br.tasks.repository;

import br.tasks.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de usuários.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo e-mail.
     *
     * @param email e-mail normalizado
     * @return usuário encontrado, se existir
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se já existe usuário com o e-mail informado.
     *
     * @param email e-mail normalizado
     * @return {@code true} quando o e-mail já está cadastrado
     */
    boolean existsByEmail(String email);
}
