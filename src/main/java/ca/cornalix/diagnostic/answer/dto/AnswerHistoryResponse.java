package ca.cornalix.diagnostic.answer.dto;

import ca.cornalix.diagnostic.answer.AnswerValue;

import java.time.Instant;
import java.util.UUID;

/** Une entree de l'historique des reponses (SCRUM-51), la plus recente en premier. */
public record AnswerHistoryResponse(
        UUID id,
        AnswerValue previousValue,
        AnswerValue newValue,
        String changedBy,
        Instant changedAt
) {
}
