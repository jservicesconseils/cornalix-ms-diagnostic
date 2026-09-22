package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.question.dto.QuestionRequest;
import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import ca.cornalix.diagnostic.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ca.cornalix.diagnostic.question.ImplementationGroup.IG1;
import static ca.cornalix.diagnostic.question.NistFunction.IDENTIFY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@Import(SecurityConfig.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static QuestionResponse sampleResponse(UUID id) {
        return new QuestionResponse(id, 1, "1.1", IDENTIFY, IG1,
                Map.of("fr", "Avez-vous un inventaire ?", "en", "Do you have an inventory?"));
    }

    private static QuestionRequest sampleRequest() {
        return new QuestionRequest(1, "1.1", IDENTIFY, IG1,
                Map.of("fr", "Avez-vous un inventaire ?", "en", "Do you have an inventory?"));
    }

    @Test
    void creerUneQuestion_avecJetonValide_renvoie201() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.create(any())).thenReturn(sampleResponse(id));

        mockMvc.perform(post("/api/v1/diagnostics/questions")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/diagnostics/questions/" + id))
                .andExpect(jsonPath("$.translations.fr").value("Avez-vous un inventaire ?"))
                .andExpect(jsonPath("$.translations.en").value("Do you have an inventory?"));
    }

    @Test
    void creerUneQuestion_sansTraduction_renvoie400() throws Exception {
        String bodyAvecTraductionsVides = """
                {"cisControl":1,"cisSafeguard":"1.1","nistFunction":"IDENTIFY","implementationGroup":"IG1","translations":{}}""";

        mockMvc.perform(post("/api/v1/diagnostics/questions")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAvecTraductionsVides))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerUneQuestion_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(post("/api/v1/diagnostics/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listerLesQuestions_avecJetonValide_renvoie200() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.listAll()).thenReturn(List.of(sampleResponse(id)));

        mockMvc.perform(get("/api/v1/diagnostics/questions").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    void listerLesQuestions_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(get("/api/v1/diagnostics/questions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lireUneQuestion_existante_renvoie200() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.getById(id)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/v1/diagnostics/questions/{id}", id).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void lireUneQuestion_inexistante_renvoie404() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.getById(id)).thenThrow(new QuestionNotFoundException(id));

        mockMvc.perform(get("/api/v1/diagnostics/questions/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void modifierUneQuestion_existante_renvoie200() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.update(eq(id), any())).thenReturn(sampleResponse(id));

        mockMvc.perform(put("/api/v1/diagnostics/questions/{id}", id)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void modifierUneQuestion_inexistante_renvoie404() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.update(eq(id), any())).thenThrow(new QuestionNotFoundException(id));

        mockMvc.perform(put("/api/v1/diagnostics/questions/{id}", id)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void supprimerUneQuestion_existante_renvoie204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/diagnostics/questions/{id}", id).with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void supprimerUneQuestion_inexistante_renvoie404() throws Exception {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new QuestionNotFoundException(id)).when(questionService).delete(id);

        mockMvc.perform(delete("/api/v1/diagnostics/questions/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }
}
