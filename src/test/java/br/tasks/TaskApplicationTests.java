package br.tasks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de contexto com H2 (perfil de teste) que valida a integração entre
 * JPA, segurança, JWT e configuração da aplicação.
 */
@SpringBootTest
@ActiveProfiles("test")
class TaskApplicationTests {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextoCarregaComSegurancaEJwtConfigurados() {
        assertNotNull(securityFilterChain);
        assertNotNull(passwordEncoder);
    }
}
