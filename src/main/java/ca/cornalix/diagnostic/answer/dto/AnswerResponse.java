package ca.cornalix.diagnostic.answer.dto;

import ca.cornalix.diagnostic.answer.AnswerValue;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie pour la reponse courante d'une organisation a une question. */
public record AnswerResponse(
        UUID id,
        UUID questionId,
        AnswerValue value,
        Instant answeredAt
) {
}
