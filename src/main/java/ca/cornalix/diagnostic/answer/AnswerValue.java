package ca.cornalix.diagnostic.answer;

/**
 * Valeur d'une reponse a une question du questionnaire. Une enumeration a
 * 4 valeurs plutot qu'un simple booleen -- convention courante des
 * auto-evaluations CIS/NIST, necessaire pour un futur calcul de score
 * nuance (PARTIAL = credit partiel, NOT_APPLICABLE = exclu du calcul).
 */
public enum AnswerValue {
    YES,
    NO,
    PARTIAL,
    NOT_APPLICABLE
}
