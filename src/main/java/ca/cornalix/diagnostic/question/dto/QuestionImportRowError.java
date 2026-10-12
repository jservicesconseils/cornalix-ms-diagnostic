package ca.cornalix.diagnostic.question.dto;

/** Une ligne du CSV importe qui n'a pas pu etre traitee, et pourquoi. */
public record QuestionImportRowError(
        int rowNumber,
        String message
) {
}
