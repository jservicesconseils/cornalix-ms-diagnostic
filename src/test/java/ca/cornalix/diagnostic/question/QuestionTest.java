package ca.cornalix.diagnostic.question;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static ca.cornalix.diagnostic.question.ImplementationGroup.IG1;
import static ca.cornalix.diagnostic.question.NistFunction.IDENTIFY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuestionTest {

    @Test
    void replaceTranslations_langueDejaPresente_metAJourEnPlaceSansRecreer() {
        Question question = new Question(1, "1.1", IDENTIFY, IG1);
        question.replaceTranslations(Map.of("fr", "ancien texte"));
        QuestionTranslation original = question.getTranslations().get(0);

        question.replaceTranslations(Map.of("fr", "nouveau texte"));

        assertEquals(1, question.getTranslations().size());
        assertEquals("nouveau texte", question.getTranslations().get(0).getText());
        // meme instance mutee, pas une nouvelle ligne -- c'est ce qui evite
        // le conflit INSERT-avant-DELETE sur la contrainte unique en base
        assertSame(original, question.getTranslations().get(0));
    }

    @Test
    void replaceTranslations_langueRetiree_estSupprimeeDeLaCollection() {
        Question question = new Question(1, "1.1", IDENTIFY, IG1);
        question.replaceTranslations(Map.of("fr", "texte", "en", "text"));

        question.replaceTranslations(Map.of("fr", "texte"));

        assertEquals(1, question.getTranslations().size());
        assertEquals("fr", question.getTranslations().get(0).getLocale());
    }

    @Test
    void replaceTranslations_nouvelleLangue_estAjoutee() {
        Question question = new Question(1, "1.1", IDENTIFY, IG1);
        question.replaceTranslations(Map.of("fr", "texte"));

        question.replaceTranslations(Map.of("fr", "texte", "en", "text"));

        assertEquals(2, question.getTranslations().size());
    }
}
