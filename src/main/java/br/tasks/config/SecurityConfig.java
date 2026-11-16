package br.tasks.config;

import br.tasks.security.JwtAuthenticationFilter;
import br.tasks.security.RestAccessDeniedHandler;
import br.tasks.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança: API stateless, JWT, BCrypt e liberação do Swagger.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/",
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/favicon.ico",
            "/error"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * Construtor com injeção de dependência.
     *
     * @param jwtAuthenticationFilterParam filtro JWT
     * @param authenticationEntryPointParam tratamento de 401
     * @param accessDeniedHandlerParam tratamento de 403
     */
    public SecurityConfig(final JwtAuthenticationFilter jwtAuthenticationFilterParam,
                          final RestAuthenticationEntryPoint authenticationEntryPointParam,
                          final RestAccessDeniedHandler accessDeniedHandlerParam) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilterParam;
        this.authenticationEntryPoint = authenticationEntryPointParam;
        this.accessDeniedHandler = accessDeniedHandlerParam;
    }

    /**
     * Cadeia de filtros da aplicação.
     *
     * @param http configuração do Spring Security
     * @return cadeia configurada
     * @throws Exception em caso de configuração inválida
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Codificador de senhas BCrypt.
     *
     * @return encoder BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Gerenciador de autenticação utilizado no login.
     *
     * @param configuration configuração do Spring Security
     * @return gerenciador de autenticação
     * @throws Exception em caso de configuração inválida
     */
    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configuração de CORS para permitir o uso do Swagger e clientes web.
     *
     * @return fonte de configuração de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origens = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList();

        if (origens.contains("*") || origens.isEmpty()) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(origens);
        }

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
