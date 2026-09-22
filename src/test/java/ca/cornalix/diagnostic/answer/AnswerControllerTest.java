package ca.cornalix.diagnostic.answer;

import ca.cornalix.diagnostic.answer.dto.AnswerRequest;
import ca.cornalix.diagnostic.answer.dto.AnswerResponse;
import ca.cornalix.diagnostic.question.QuestionNotFoundException;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnswerController.class)
@Import(SecurityConfig.class)
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnswerService answerService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static AnswerResponse sampleResponse(UUID questionId) {
        return new AnswerResponse(UUID.randomUUID(), questionId, AnswerValue.YES, Instant.now());
    }

    @Test
    void repondre_avecTenantIdCorrespondant_renvoie200() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        when(answerService.upsert(eq(orgId), eq(questionId), any())).thenReturn(sampleResponse(questionId));

        mockMvc.perform(put("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerRequest(AnswerValue.YES))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("YES"));
    }

    @Test
    void repondre_avecTenantIdDifferent_renvoie403() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerRequest(AnswerValue.YES))))
                .andExpect(status().isForbidden());
    }

    @Test
    void repondre_sansTenantId_renvoie403() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerRequest(AnswerValue.YES))))
                .andExpect(status().isForbidden());
    }

    @Test
    void repondre_sansJeton_renvoie401() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerRequest(AnswerValue.YES))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repondre_sansValeur_renvoie400() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void repondre_questionInexistante_renvoie404() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        when(answerService.upsert(eq(orgId), eq(questionId), any())).thenThrow(new QuestionNotFoundException(questionId));

        mockMvc.perform(put("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerRequest(AnswerValue.YES))))
                .andExpect(status().isNotFound());
    }

    @Test
    void lireUneReponse_existante_renvoie200() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        when(answerService.getOne(orgId, questionId)).thenReturn(sampleResponse(questionId));

        mockMvc.perform(get("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString()))))
                .andExpect(status().isOk());
    }

    @Test
    void lireUneReponse_inexistante_renvoie404() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        when(answerService.getOne(orgId, questionId)).thenThrow(new AnswerNotFoundException(orgId, questionId));

        mockMvc.perform(get("/api/v1/diagnostics/organizations/{orgId}/answers/{qId}", orgId, questionId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerLesReponses_avecTenantIdCorrespondant_renvoie200() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(answerService.listForOrganization(orgId)).thenReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/api/v1/diagnostics/organizations/{orgId}/answers", orgId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", orgId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listerLesReponses_consultantAvecTenantScopeCouvrant_renvoie200() throws Exception {
        UUID tenantPrincipal = UUID.randomUUID();
        UUID autreOrganisation = UUID.randomUUID();
        when(answerService.listForOrganization(autreOrganisation)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/diagnostics/organizations/{orgId}/answers", autreOrganisation)
                        .with(jwt().jwt(builder -> builder
                                .claim("tenant_id", tenantPrincipal.toString())
                                .claim("tenant_scope", "[\"" + tenantPrincipal + "\",\"" + autreOrganisation + "\"]"))))
                .andExpect(status().isOk());
    }
}
