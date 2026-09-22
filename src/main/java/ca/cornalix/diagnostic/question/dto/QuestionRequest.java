package ca.cornalix.diagnostic.question.dto;

import ca.cornalix.diagnostic.question.ImplementationGroup;
import ca.cornalix.diagnostic.question.NistFunction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Ce que le client envoie pour creer ou remplacer une question. Utilise
 * pour la creation ET la mise a jour -- meme forme dans les deux cas.
 *
 * {@code translations} : au moins une entree, cle = code de langue (ex.
 * "fr", "en"), valeur = le texte. Le format precis de la cle (2 lettres
 * minuscules) est verifie en service, pas ici (Bean Validation ne
 * valide pas facilement le motif des cles d'une Map).
 */
public record QuestionRequest(

        @Min(value = 1, message = "le controle CIS doit etre entre 1 et 18")
        @Max(value = 18, message = "le controle CIS doit etre entre 1 et 18")
        int cisControl,

        @NotBlank(message = "le safeguard CIS est requis")
        String cisSafeguard,

        @NotNull(message = "la fonction NIST CSF est requise")
        NistFunction nistFunction,

        @NotNull(message = "le groupe d'implementation CIS est requis")
        ImplementationGroup implementationGroup,

        @NotEmpty(message = "au moins une traduction est requise")
        Map<String, @NotBlank(message = "le texte d'une traduction ne peut pas etre vide") String> translations
) {
}
