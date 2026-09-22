package ca.cornalix.diagnostic.answer;

import ca.cornalix.diagnostic.answer.dto.AnswerRequest;
import ca.cornalix.diagnostic.answer.dto.AnswerResponse;
import ca.cornalix.diagnostic.security.TenantClaims;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Meme forme que /api/v1/organizations/{id} sur cornalix-ms-identity
 * (SCRUM-6) : le tenant_id/tenant_scope de l'appelant est verifie contre
 * {organizationId} avant tout acces -- couvre deja le cas Consultant
 * multi-tenant sans traitement special.
 */
@RestController
@RequestMapping("/api/v1/diagnostics/organizations/{organizationId}/answers")
public class AnswerController {

    private final AnswerService service;

    public AnswerController(AnswerService service) {
        this.service = service;
    }

    @PutMapping("/{questionId}")
    public AnswerResponse upsert(@PathVariable UUID organizationId, @PathVariable UUID questionId,
                                  @Valid @RequestBody AnswerRequest request, @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return service.upsert(organizationId, questionId, request.value());
    }

    @GetMapping("/{questionId}")
    public AnswerResponse getOne(@PathVariable UUID organizationId, @PathVariable UUID questionId,
                                  @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return service.getOne(organizationId, questionId);
    }

    @GetMapping
    public List<AnswerResponse> list(@PathVariable UUID organizationId, @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return service.listForOrganization(organizationId);
    }
}
