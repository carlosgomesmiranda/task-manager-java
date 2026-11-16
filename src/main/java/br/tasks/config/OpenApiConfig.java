package br.tasks.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI/Swagger com esquema de segurança Bearer JWT.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Metadados da API e esquema de autenticação Bearer.
     *
     * @return documento OpenAPI
     */
    @Bean
    public OpenAPI taskManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskManager API")
                        .description("API REST para gerenciamento de tarefas com autenticação JWT. "
                                + "Cada usuário acessa apenas as suas próprias tarefas.")
                        .version("1.0.0")
                        .contact(new Contact().name("TaskManager"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Ambiente local")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe somente o token JWT. O Swagger adiciona o prefixo Bearer.")));
    }
}
