package ca.cornalix.diagnostic.answer;

import java.util.UUID;

/**
 * Levee quand l'organisation n'a pas encore repondu a la question demandee.
 * Traduite en reponse HTTP 404 (Not Found) par AnswerExceptionHandler.
 */
public class AnswerNotFoundException extends RuntimeException {

    public AnswerNotFoundException(UUID organizationId, UUID questionId) {
        super("Aucune reponse de l'organisation " + organizationId + " pour la question " + questionId);
    }
}
