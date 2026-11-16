package br.tasks;

import br.tasks.dto.auth.LoginRequest;
import br.tasks.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração do fluxo completo: cadastro, login JWT, CRUD e regras
 * de propriedade das tarefas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TarefaApiIntegrationTest {

    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveExecutarFluxoCompletoComSegurancaEProprietario() throws Exception {
        String emailMaria = "maria." + UUID.randomUUID() + "@example.com";
        String emailJoao = "joao." + UUID.randomUUID() + "@example.com";

        registrar("Maria", emailMaria);
        registrar("João", emailJoao);

        String tokenMaria = login(emailMaria);
        String tokenJoao = login(emailJoao);

        mockMvc.perform(get("/api/v1/tarefas"))
                .andExpect(status().isUnauthorized());

        MvcResult criacao = mockMvc.perform(post("/api/v1/tarefas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Estudar Spring Security",
                                  "prioridade": "ALTA",
                                  "concluida": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Estudar Spring Security"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.concluida").value(false))
                .andExpect(jsonPath("$.usuario.email").value(emailMaria))
                .andReturn();

        long id = objectMapper.readTree(criacao.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/v1/tarefas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJoao))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJoao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Tentativa indevida",
                                  "prioridade": "BAIXA",
                                  "concluida": true
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJoao))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(put("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Estudar JWT",
                                  "prioridade": "MEDIA",
                                  "concluida": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Estudar JWT"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.concluida").value(true));

        mockMvc.perform(get("/api/v1/tarefas")
                        .param("concluida", "true")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/v1/tarefas")
                        .param("concluida", "false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(post("/api/v1/tarefas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "   ",
                                  "prioridade": "ALTA",
                                  "concluida": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.descricao").exists());

        mockMvc.perform(delete("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tarefas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMaria))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarCadastroDuplicadoELoginInvalido() throws Exception {
        String email = "duplicado." + UUID.randomUUID() + "@example.com";

        registrar("Duplicado", email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Duplicado", email, SENHA))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("existe")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(email, "senha-errada"))))
                .andExpect(status().isUnauthorized());
    }

    private void registrar(final String nome, final String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(nome, email, SENHA))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));
    }

    private String login(final String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(email, SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("token").asText();
    }
}
