package ca.cornalix.diagnostic.answer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ne gere que AnswerNotFoundException. QuestionNotFoundException (leve par
 * AnswerService quand on repond a une question inexistante) et
 * MethodArgumentNotValidException (@Valid sur AnswerRequest) sont deja
 * couverts globalement par QuestionExceptionHandler -- @RestControllerAdvice
 * s'applique a toute l'application, pas juste a son package. Les dupliquer
 * ici creerait une resolution ambigue entre deux beans d'advice pour le
 * meme type d'exception.
 */
@RestControllerAdvice
public class AnswerExceptionHandler {

    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAnswerNotFound(AnswerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
