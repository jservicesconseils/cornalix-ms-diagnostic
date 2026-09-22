package ca.cornalix.diagnostic.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les erreurs d'autorisation liees au tenant en reponses HTTP
 * propres. Separe des handlers de domaine (organisation/question/answer) :
 * ce n'est pas une regle metier d'un domaine particulier, mais une
 * garantie transverse qui s'applique a toute route scopee par tenant.
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleTenantAccessDenied(TenantAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(ex.getMessage()));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
