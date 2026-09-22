package ca.cornalix.diagnostic.question.dto;

import ca.cornalix.diagnostic.question.ImplementationGroup;
import ca.cornalix.diagnostic.question.NistFunction;

import java.util.Map;
import java.util.UUID;

/**
 * Ce que l'API renvoie pour une question du catalogue de diagnostic.
 * {@code translations} porte toutes les langues disponibles (ex.
 * {"fr": "...", "en": "..."}) -- pas de filtrage par langue a ce stade.
 */
public record QuestionResponse(
        UUID id,
        int cisControl,
        String cisSafeguard,
        NistFunction nistFunction,
        ImplementationGroup implementationGroup,
        Map<String, String> translations
) {
}
