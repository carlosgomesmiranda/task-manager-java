package br.tasks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Geração e validação dos tokens JWT (HMAC-SHA).
 */
@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final long expiracaoMs;

    /**
     * Cria o serviço com o segredo e a validade configurados.
     *
     * @param segredo     segredo HMAC (mínimo de 32 bytes)
     * @param expiracaoMs validade do token em milissegundos
     */
    public JwtService(@Value("${app.jwt.secret}") final String segredo,
                      @Value("${app.jwt.expiration-ms}") final long expiracaoMs) {
        if (segredo == null
                || segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "app.jwt.secret deve possuir pelo menos " + TAMANHO_MINIMO_SEGREDO + " bytes");
        }
        if (expiracaoMs <= 0) {
            throw new IllegalStateException("app.jwt.expiration-ms deve ser maior que zero");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    /**
     * Gera o token JWT de um usuário autenticado.
     *
     * @param principal usuário autenticado
     * @return token assinado
     */
    public String gerarToken(final UsuarioPrincipal principal) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("nome", principal.getNome())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    /**
     * Extrai o e-mail (subject) do token.
     *
     * @param token token JWT
     * @return e-mail contido no token
     * @throws JwtException quando o token é inválido ou está expirado
     */
    public String extrairEmail(final String token) {
        return extrairClaims(token).getSubject();
    }

    /**
     * Valida a assinatura, a expiração e o usuário do token.
     *
     * @param token       token JWT
     * @param userDetails usuário esperado
     * @return {@code true} quando o token é válido para o usuário
     */
    public boolean tokenValido(final String token, final UserDetails userDetails) {
        try {
            Claims claims = extrairClaims(token);
            return claims.getSubject() != null
                    && claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Validade configurada do token em segundos.
     *
     * @return validade em segundos
     */
    public long getExpiracaoEmSegundos() {
        return expiracaoMs / 1000L;
    }

    private Claims extrairClaims(final String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
