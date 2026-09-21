package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SCRUM-10 : le catalogue est statique et partage -- pas encore de
 * filtrage par tenant ni par groupe IG (tranche d'employes de
 * l'organisation). Voir la portee du recit pour le contexte.
 */
@Service
public class QuestionService {

    private final QuestionRepository repository;

    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
    }

    public List<QuestionResponse> listAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getCisControl(),
                question.getCisSafeguard(),
                question.getNistFunction(),
                question.getImplementationGroup()
        );
    }
}
