package br.tasks.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro responsável por autenticar requisições que enviam
 * {@code Authorization: Bearer <token>}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Construtor com injeção de dependência.
     *
     * @param jwtServiceParam       serviço de JWT
     * @param userDetailsServiceParam serviço de usuários
     */
    public JwtAuthenticationFilter(final JwtService jwtServiceParam,
                                   final UserDetailsService userDetailsServiceParam) {
        this.jwtService = jwtServiceParam;
        this.userDetailsService = userDetailsServiceParam;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(PREFIXO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIXO_BEARER.length()).trim();

        try {
            String email = jwtService.extrairEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.tokenValido(token, userDetails)) {
                    UsernamePasswordAuthenticationToken autenticacao =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    autenticacao.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            LOGGER.debug("Token JWT rejeitado: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
