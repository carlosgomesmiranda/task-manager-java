package br.tasks.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Redireciona a raiz da aplicação para a documentação Swagger.
 */
@Hidden
@RestController
public class HomeController {

    /**
     * Redireciona {@code /} para o Swagger UI.
     *
     * @return resposta 302 com o destino da documentação
     */
    @GetMapping("/")
    public ResponseEntity<Void> home() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/swagger-ui/index.html"))
                .build();
    }
}
