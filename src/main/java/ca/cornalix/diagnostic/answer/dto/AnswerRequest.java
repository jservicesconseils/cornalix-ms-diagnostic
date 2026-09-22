package ca.cornalix.diagnostic.answer.dto;

import ca.cornalix.diagnostic.answer.AnswerValue;
import jakarta.validation.constraints.NotNull;

/** Ce que le client envoie pour repondre (ou corriger sa reponse) a une question. */
public record AnswerRequest(

        @NotNull(message = "la valeur de la reponse est requise")
        AnswerValue value
) {
}
