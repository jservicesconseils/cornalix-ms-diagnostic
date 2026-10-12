package ca.cornalix.diagnostic.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    // Cle naturelle d'une question CIS (ecran d'administration, import
    // CSV) -- (cisControl, cisSafeguard) identifie la meme question d'un
    // import a l'autre, contrairement a l'id interne que l'auteur d'un
    // fichier CSV ne connait pas.
    Optional<Question> findByCisControlAndCisSafeguard(int cisControl, String cisSafeguard);
}
