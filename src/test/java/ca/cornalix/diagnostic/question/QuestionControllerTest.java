package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import ca.cornalix.diagnostic.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static ca.cornalix.diagnostic.question.ImplementationGroup.IG1;
import static ca.cornalix.diagnostic.question.NistFunction.IDENTIFY;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de la tranche web (@WebMvcTest), meme approche que
 * OrganizationControllerTest sur cornalix-ms-identity : securite reelle
 * importee, JwtDecoder mocke (jamais appele -- jwt() injecte directement
 * un principal authentifie), service mocke pour isoler le controleur.
 */
@WebMvcTest(QuestionController.class)
@Import(SecurityConfig.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void listerLesQuestions_avecJetonValide_renvoie200EtLeCatalogue() throws Exception {
        UUID id = UUID.randomUUID();
        when(questionService.listAll()).thenReturn(List.of(
                new QuestionResponse(id, "Disposez-vous d'un inventaire des appareils mobiles ?", 1, "1.1", IDENTIFY, IG1)
        ));

        mockMvc.perform(get("/api/v1/diagnostics/questions").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].cisControl").value(1))
                .andExpect(jsonPath("$[0].cisSafeguard").value("1.1"))
                .andExpect(jsonPath("$[0].nistFunction").value("IDENTIFY"))
                .andExpect(jsonPath("$[0].implementationGroup").value("IG1"));
    }

    @Test
    void listerLesQuestions_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(get("/api/v1/diagnostics/questions"))
                .andExpect(status().isUnauthorized());
    }
}
