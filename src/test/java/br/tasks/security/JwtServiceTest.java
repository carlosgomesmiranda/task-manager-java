package br.tasks.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SEGREDO =
            "segredo-de-teste-com-mais-de-32-caracteres-para-hmac-sha-256";

    private final JwtService jwtService = new JwtService(SEGREDO, 3_600_000L);

    @Test
    void deveGerarExtrairEValidarToken() {
        UsuarioPrincipal principal =
                new UsuarioPrincipal(7L, "Ana", "ana@example.com", "hash");

        String token = jwtService.gerarToken(principal);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("ana@example.com", jwtService.extrairEmail(token));
        assertTrue(jwtService.tokenValido(token, principal));
        assertEquals(3600L, jwtService.getExpiracaoEmSegundos());
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        UsuarioPrincipal principal =
                new UsuarioPrincipal(7L, "Ana", "ana@example.com", "hash");
        String token = jwtService.gerarToken(principal);

        int ultimoPonto = token.lastIndexOf('.');
        char primeiroCharAssinatura = token.charAt(ultimoPonto + 1);
        char charAdulterado = primeiroCharAssinatura == 'A' ? 'B' : 'A';
        String adulterado = token.substring(0, ultimoPonto + 1)
                + charAdulterado
                + token.substring(ultimoPonto + 2);

        assertFalse(jwtService.tokenValido(adulterado, principal));
    }

    @Test
    void deveRejeitarTokenDeOutroUsuario() {
        UsuarioPrincipal ana = new UsuarioPrincipal(7L, "Ana", "ana@example.com", "hash");
        UsuarioPrincipal bruno = new UsuarioPrincipal(8L, "Bruno", "bruno@example.com", "hash");

        String token = jwtService.gerarToken(ana);

        assertFalse(jwtService.tokenValido(token, bruno));
    }

    @Test
    void deveRejeitarSegredoCurto() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("curto", 3_600_000L));
    }

    @Test
    void deveRejeitarExpiracaoInvalida() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService(SEGREDO, 0L));
    }
}
