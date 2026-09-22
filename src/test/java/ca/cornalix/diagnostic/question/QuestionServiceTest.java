package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.answer.AnswerRepository;
import ca.cornalix.diagnostic.question.dto.QuestionRequest;
import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ca.cornalix.diagnostic.question.ImplementationGroup.IG1;
import static ca.cornalix.diagnostic.question.NistFunction.IDENTIFY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
