package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.question.dto.QuestionRequest;
import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * SCRUM-11 : le catalogue est entierement pilote par la base -- aucun
 * contenu code en dur (voir retrait de QuestionCatalogSeeder, SCRUM-10).
 *
 * @Transactional sur la classe : "translations" (Question) est une
 * collection LAZY -- sans transaction ouverte pendant tout l'appel,
 * toResponse() leve LazyInitializationException des qu'il essaie de la
 * lire (la session Hibernate est deja fermee une fois le repository
 * revenu).
 */
@Service
@Transactional
public class QuestionService {

    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2}$");

    private final QuestionRepository repository;

    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
    }

    public QuestionResponse create(QuestionRequest request) {
        validateTranslations(request.translations());

        Question question = new Question(
                request.cisControl(), request.cisSafeguard(), request.nistFunction(), request.implementationGroup());
        question.replaceTranslations(request.translations());

        return toResponse(repository.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public QuestionResponse update(UUID id, QuestionRequest request) {
        validateTranslations(request.translations());

        Question question = findOrThrow(id);
        question.setCisControl(request.cisControl());
        question.setCisSafeguard(request.cisSafeguard());
        question.setNistFunction(request.nistFunction());
        question.setImplementationGroup(request.implementationGroup());
        question.replaceTranslations(request.translations());

        return toResponse(repository.save(question));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new QuestionNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Question findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));
    }

    private void validateTranslations(Map<String, String> translations) {
        for (String locale : translations.keySet()) {
            if (!LOCALE_PATTERN.matcher(locale).matches()) {
                throw new InvalidTranslationsException(
                        "code de langue invalide : '" + locale + "' (attendu : 2 lettres minuscules, ex. 'fr', 'en')");
            }
        }
    }

    private QuestionResponse toResponse(Question question) {
        Map<String, String> translations = new LinkedHashMap<>();
        question.getTranslations().forEach(t -> translations.put(t.getLocale(), t.getText()));

        return new QuestionResponse(
                question.getId(),
                question.getCisControl(),
                question.getCisSafeguard(),
                question.getNistFunction(),
                question.getImplementationGroup(),
                translations
        );
    }
}
