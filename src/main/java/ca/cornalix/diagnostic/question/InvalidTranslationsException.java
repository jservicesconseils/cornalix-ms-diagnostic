package ca.cornalix.diagnostic.question;

/**
 * Levee quand les traductions fournies pour une question sont invalides
 * (aucune traduction, code de langue mal forme, ou texte vide).
 * Traduite en reponse HTTP 400 (Bad Request) par QuestionExceptionHandler.
 */
public class InvalidTranslationsException extends RuntimeException {

    public InvalidTranslationsException(String message) {
        super(message);
    }
}
