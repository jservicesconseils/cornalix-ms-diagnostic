package ca.cornalix.diagnostic.question.dto;

import ca.cornalix.diagnostic.question.ImplementationGroup;
import ca.cornalix.diagnostic.question.NistFunction;

import java.util.UUID;

/** Ce que l'API renvoie pour une question du catalogue de diagnostic. */
public record QuestionResponse(
        UUID id,
        String text,
        int cisControl,
        String cisSafeguard,
        NistFunction nistFunction,
        ImplementationGroup implementationGroup
) {
}
