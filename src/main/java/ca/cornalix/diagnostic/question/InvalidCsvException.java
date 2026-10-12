package ca.cornalix.diagnostic.question;

/**
 * Levee quand le fichier CSV importe (ecran d'administration des
 * questions) est illisible dans son ensemble (encodage, en-tete absent
 * ou colonnes manquantes) -- distinct des erreurs ligne par ligne, qui
 * n'interrompent pas l'import (voir QuestionImportResult.errors()).
 * Traduite en reponse HTTP 400 par QuestionExceptionHandler.
 */
public class InvalidCsvException extends RuntimeException {

    public InvalidCsvException(String message) {
        super(message);
    }
}
