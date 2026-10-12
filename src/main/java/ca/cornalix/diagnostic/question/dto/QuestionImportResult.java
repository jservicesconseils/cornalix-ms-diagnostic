package ca.cornalix.diagnostic.question.dto;

import java.util.List;

/**
 * Resume d'un import CSV (ecran d'administration des questions).
 * L'import traite toutes les lignes meme si certaines echouent -- une
 * ligne malformee ne doit pas faire echouer les 199 autres.
 */
public record QuestionImportResult(
        int created,
        int updated,
        List<QuestionImportRowError> errors
) {
}
