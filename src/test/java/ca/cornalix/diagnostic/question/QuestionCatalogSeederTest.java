package ca.cornalix.diagnostic.question;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifie que le seed s'est reellement execute au demarrage (contexte
 * Spring complet) et couvre bien les 4 domaines vises par SCRUM-10.
 */
@SpringBootTest
class QuestionCatalogSeederTest {

    @Autowired
    private QuestionRepository repository;

    @Test
    void leCatalogueEstSeedeAuDemarrage_etCouvreLes4Domaines() {
        var questions = repository.findAll();

        assertFalse(questions.isEmpty());

        Set<Integer> cisControls = questions.stream().map(Question::getCisControl).collect(java.util.stream.Collectors.toSet());
        assertTrue(cisControls.contains(12) || cisControls.contains(13), "reseau");
        assertTrue(cisControls.contains(2) || cisControls.contains(7), "logiciels");
        assertTrue(cisControls.contains(1) || cisControls.contains(4), "appareils mobiles");
        assertTrue(cisControls.contains(16), "CI/CD");
    }

    @Test
    void leSeedEstIdempotent_neDoublePasAuRedemarrage() {
        long avant = repository.count();

        new QuestionCatalogSeeder(repository).run(null);

        assertEquals(avant, repository.count());
    }
}
