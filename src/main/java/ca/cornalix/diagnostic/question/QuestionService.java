package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.answer.AnswerRepository;
import ca.cornalix.diagnostic.common.PageResponse;
import ca.cornalix.diagnostic.question.dto.QuestionImportResult;
import ca.cornalix.diagnostic.question.dto.QuestionImportRowError;
import ca.cornalix.diagnostic.question.dto.QuestionRequest;
import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    // text_en volontairement absente : seule traduction optionnelle du
    // CSV d'import (voir parseRow).
    private static final List<String> REQUIRED_CSV_COLUMNS =
            List.of("cisControl", "cisSafeguard", "nistFunction", "implementationGroup", "text_fr");

    private final QuestionRepository repository;
    private final AnswerRepository answerRepository;

    public QuestionService(QuestionRepository repository, AnswerRepository answerRepository) {
        this.repository = repository;
        this.answerRepository = answerRepository;
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
    public PageResponse<QuestionResponse> listPaged(int page, int size) {
        return PageResponse.from(repository.findAll(PageRequest.of(page, size)).map(this::toResponse));
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

    // Import en masse (ecran d'administration des questions) : upsert
    // par (cisControl, cisSafeguard) -- cette paire identifie la meme
    // question d'un import a l'autre, contrairement a l'id interne que
    // l'auteur d'un fichier CSV ne connait pas. Une ligne malformee
    // n'interrompt jamais les suivantes : erreur collectee, le reste du
    // fichier continue d'etre traite.
    public QuestionImportResult importFromCsv(InputStream csvInput) {
        List<QuestionImportRowError> errors = new ArrayList<>();
        int created = 0;
        int updated = 0;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (Reader reader = new InputStreamReader(csvInput, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            List<String> missingColumns = REQUIRED_CSV_COLUMNS.stream()
                    .filter(column -> !parser.getHeaderNames().contains(column))
                    .toList();
            if (!missingColumns.isEmpty()) {
                throw new InvalidCsvException("Colonne(s) manquante(s) dans l'en-tete du CSV : " + String.join(", ", missingColumns));
            }

            for (CSVRecord record : parser) {
                // +1 : la premiere ligne de donnees (apres l'en-tete) est
                // la "ligne 2" pour quelqu'un qui ouvre le fichier dans un
                // tableur -- numero plus utile qu'un index base sur 0.
                int rowNumber = (int) record.getRecordNumber() + 1;
                try {
                    if (importRow(parseRow(record))) {
                        updated++;
                    } else {
                        created++;
                    }
                } catch (IllegalArgumentException e) {
                    errors.add(new QuestionImportRowError(rowNumber, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new InvalidCsvException("Impossible de lire le fichier CSV : " + e.getMessage());
        } catch (IllegalStateException e) {
            // commons-csv leve IllegalStateException sur un en-tete
            // dedouble ou un CSV structurellement invalide (pas une
            // erreur ligne par ligne).
            throw new InvalidCsvException("Fichier CSV invalide : " + e.getMessage());
        }

        return new QuestionImportResult(created, updated, errors);
    }

    /** @return true si une question existante a ete mise a jour, false si une nouvelle a ete creee. */
    private boolean importRow(ParsedRow row) {
        Optional<Question> existing = repository.findByCisControlAndCisSafeguard(row.cisControl(), row.cisSafeguard());

        if (existing.isPresent()) {
            Question question = existing.get();
            question.setNistFunction(row.nistFunction());
            question.setImplementationGroup(row.implementationGroup());
            question.replaceTranslations(row.translations());
            repository.save(question);
            return true;
        }

        Question question = new Question(row.cisControl(), row.cisSafeguard(), row.nistFunction(), row.implementationGroup());
        question.replaceTranslations(row.translations());
        repository.save(question);
        return false;
    }

    private record ParsedRow(
            int cisControl, String cisSafeguard, NistFunction nistFunction,
            ImplementationGroup implementationGroup, Map<String, String> translations) {
    }

    // Colonnes attendues : cisControl, cisSafeguard, nistFunction,
    // implementationGroup, text_fr, text_en (text_en optionnelle).
    private ParsedRow parseRow(CSVRecord record) {
        String rawCisControl = column(record, "cisControl");
        int cisControl;
        try {
            cisControl = Integer.parseInt(rawCisControl);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("cisControl invalide (nombre entre 1 et 18 attendu) : '" + rawCisControl + "'");
        }
        if (cisControl < 1 || cisControl > 18) {
            throw new IllegalArgumentException("cisControl doit etre entre 1 et 18 : " + cisControl);
        }

        String cisSafeguard = column(record, "cisSafeguard");
        if (cisSafeguard.isBlank()) {
            throw new IllegalArgumentException("cisSafeguard est requis");
        }

        String rawNistFunction = column(record, "nistFunction");
        NistFunction nistFunction;
        try {
            nistFunction = NistFunction.valueOf(rawNistFunction.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "nistFunction invalide : '" + rawNistFunction + "' (attendu : GOVERN, IDENTIFY, PROTECT, DETECT, RESPOND ou RECOVER)");
        }

        String rawIg = column(record, "implementationGroup");
        ImplementationGroup implementationGroup;
        try {
            implementationGroup = ImplementationGroup.valueOf(rawIg.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("implementationGroup invalide : '" + rawIg + "' (attendu : IG1, IG2 ou IG3)");
        }

        String textFr = column(record, "text_fr");
        if (textFr.isBlank()) {
            throw new IllegalArgumentException("text_fr est requis");
        }

        Map<String, String> translations = new LinkedHashMap<>();
        translations.put("fr", textFr);
        if (record.isMapped("text_en")) {
            String textEn = record.get("text_en");
            if (textEn != null && !textEn.isBlank()) {
                translations.put("en", textEn);
            }
        }

        return new ParsedRow(cisControl, cisSafeguard, nistFunction, implementationGroup, translations);
    }

    private String column(CSVRecord record, String name) {
        if (!record.isMapped(name)) {
            throw new IllegalArgumentException("colonne manquante dans l'en-tete du CSV : '" + name + "'");
        }
        return record.get(name);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new QuestionNotFoundException(id);
        }
        if (answerRepository.existsByQuestionId(id)) {
            throw new QuestionHasAnswersException(id);
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
