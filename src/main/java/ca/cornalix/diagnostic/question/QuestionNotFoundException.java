package ca.cornalix.diagnostic.question;

import java.util.UUID;

/**
 * Levee quand aucune question ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par QuestionExceptionHandler.
 */
public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(UUID id) {
        super("Aucune question trouvee avec l'id " + id);
    }
}
