package ca.cornalix.diagnostic.question;

import java.util.UUID;

/**
 * Levee quand on tente de supprimer une question a laquelle au moins une
 * organisation a deja repondu. Traduite en reponse HTTP 409 (Conflict)
 * par QuestionExceptionHandler -- refuse explicitement plutot que de
 * supprimer silencieusement les reponses des organisations (voir
 * SCRUM-13).
 */
public class QuestionHasAnswersException extends RuntimeException {

    public QuestionHasAnswersException(UUID id) {
        super("Impossible de supprimer la question " + id + " : au moins une organisation y a deja repondu");
    }
}
