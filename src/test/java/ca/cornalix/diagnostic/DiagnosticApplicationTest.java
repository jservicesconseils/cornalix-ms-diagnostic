package ca.cornalix.diagnostic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifie que le contexte Spring demarre correctement (config, securite,
 * JPA) -- rien de plus tant qu'il n'y a pas encore de fonctionnalite metier.
 */
@SpringBootTest
class DiagnosticApplicationTest {

    @Test
    void contextLoads() {
    }
}
