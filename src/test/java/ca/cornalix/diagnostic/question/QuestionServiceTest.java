package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.answer.AnswerRepository;
import ca.cornalix.diagnostic.common.PageResponse;
import ca.cornalix.diagnostic.question.dto.QuestionImportResult;
import ca.cornalix.diagnostic.question.dto.QuestionRequest;
import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ca.cornalix.diagnostic.question.ImplementationGroup.IG1;
import static ca.cornalix.diagnostic.question.NistFunction.IDENTIFY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository repository;

    @Mock
    private AnswerRepository answerRepository;

    private QuestionService service;

    private QuestionService service() {
        return new QuestionService(repository, answerRepository);
    }

    @Test
    void create_avecTraductionsValides_sauvegardeEtRenvoieLaQuestion() {
        service = service();
        QuestionRequest request = new QuestionRequest(1, "1.1", IDENTIFY, IG1,
                Map.of("fr", "Avez-vous un inventaire ?", "en", "Do you have an inventory?"));

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponse response = service.create(request);

        assertEquals(1, response.cisControl());
        assertEquals("1.1", response.cisSafeguard());
        assertEquals(Map.of("fr", "Avez-vous un inventaire ?", "en", "Do you have an inventory?"), response.translations());
    }

    @Test
    void create_avecCodeDeLangueInvalide_leveInvalidTranslationsException() {
        service = service();
        QuestionRequest request = new QuestionRequest(1, "1.1", IDENTIFY, IG1, Map.of("french", "texte"));

        assertThrows(InvalidTranslationsException.class, () -> service.create(request));
    }

    @Test
    void listPaged_delegueAuRepositoryEtConvertitLaPage() {
        service = service();
        Question question = new Question(1, "1.1", IDENTIFY, IG1);
        question.replaceTranslations(Map.of("fr", "texte"));
        when(repository.findAll(PageRequest.of(1, 5)))
                .thenReturn(new PageImpl<>(List.of(question), PageRequest.of(1, 5), 11));

        PageResponse<QuestionResponse> response = service.listPaged(1, 5);

        assertEquals(1, response.page());
        assertEquals(5, response.size());
        assertEquals(11, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.content().size());
    }

    @Test
    void getById_questionInexistante_leveQuestionNotFoundException() {
        service = service();
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(QuestionNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void update_remplaceLesChampsEtLesTraductions() {
        service = service();
        UUID id = UUID.randomUUID();
        Question existing = new Question(1, "1.1", IDENTIFY, IG1);
        existing.replaceTranslations(Map.of("fr", "ancien texte"));
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionRequest request = new QuestionRequest(16, "16.7", NistFunction.PROTECT, ImplementationGroup.IG2,
                Map.of("fr", "nouveau texte"));

        QuestionResponse response = service.update(id, request);

        assertEquals(16, response.cisControl());
        assertEquals(Map.of("fr", "nouveau texte"), response.translations());
    }

    @Test
    void importFromCsv_nouvellesQuestions_creeToutesEtRenvoieLeCompte() {
        service = service();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // findByCisControlAndCisSafeguard non stubbe -- Mockito renvoie
        // Optional.empty() par defaut, donc chaque ligne est traitee
        // comme une nouvelle question.

        InputStream csv = csv(
                "cisControl,cisSafeguard,nistFunction,implementationGroup,text_fr,text_en",
                "1,1.1,IDENTIFY,IG1,Avez-vous un inventaire ?,Do you have an inventory?",
                "4,4.1,PROTECT,IG1,Configurations securisees appliquees ?,"
        );

        QuestionImportResult result = service.importFromCsv(csv);

        assertEquals(2, result.created());
        assertEquals(0, result.updated());
        assertEquals(List.of(), result.errors());
    }

    @Test
    void importFromCsv_questionExistante_metAJourEnPlaceSansRecreer() {
        service = service();
        Question existing = new Question(1, "1.1", IDENTIFY, IG1);
        existing.replaceTranslations(Map.of("fr", "ancien texte"));
        when(repository.findByCisControlAndCisSafeguard(1, "1.1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InputStream csv = csv(
                "cisControl,cisSafeguard,nistFunction,implementationGroup,text_fr,text_en",
                "1,1.1,IDENTIFY,IG1,nouveau texte,"
        );

        QuestionImportResult result = service.importFromCsv(csv);

        assertEquals(0, result.created());
        assertEquals(1, result.updated());
        verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.argThat(q -> q != existing));
    }

    @Test
    void importFromCsv_ligneInvalide_collecteLErreurEtContinueAvecLesSuivantes() {
        service = service();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InputStream csv = csv(
                "cisControl,cisSafeguard,nistFunction,implementationGroup,text_fr,text_en",
                "1,1.1,PAS_UNE_FONCTION,IG1,texte,",
                "2,2.1,IDENTIFY,IG1,texte valide,"
        );

        QuestionImportResult result = service.importFromCsv(csv);

        assertEquals(1, result.created());
        assertEquals(1, result.errors().size());
        assertEquals(2, result.errors().get(0).rowNumber());
        assertTrue(result.errors().get(0).message().contains("nistFunction"));
    }

    @Test
    void importFromCsv_colonneManquante_leveInvalidCsvException() {
        service = service();

        InputStream csv = csv(
                "cisControl,cisSafeguard,nistFunction,text_fr",
                "1,1.1,IDENTIFY,texte"
        );

        assertThrows(InvalidCsvException.class, () -> service.importFromCsv(csv));
    }

    private static InputStream csv(String... lines) {
        String content = String.join("\n", lines);
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void delete_questionExistante_supprimeViaLeRepository() {
        service = service();
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(repository).deleteById(captor.capture());
        assertEquals(id, captor.getValue());
    }

    @Test
    void delete_questionInexistante_leveQuestionNotFoundException() {
        service = service();
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(QuestionNotFoundException.class, () -> service.delete(id));
    }

    @Test
    void delete_questionDejaRepondue_leveQuestionHasAnswersExceptionEtNeSupprimePas() {
        service = service();
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        when(answerRepository.existsByQuestionId(id)).thenReturn(true);

        assertThrows(QuestionHasAnswersException.class, () -> service.delete(id));

        verify(repository, org.mockito.Mockito.never()).deleteById(any());
    }
}
