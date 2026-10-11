package ca.cornalix.diagnostic.answer;

import ca.cornalix.diagnostic.answer.dto.AnswerResponse;
import ca.cornalix.diagnostic.common.PageResponse;
import ca.cornalix.diagnostic.question.ImplementationGroup;
import ca.cornalix.diagnostic.question.NistFunction;
import ca.cornalix.diagnostic.question.Question;
import ca.cornalix.diagnostic.question.QuestionNotFoundException;
import ca.cornalix.diagnostic.question.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    private AnswerService service() {
        return new AnswerService(answerRepository, questionRepository);
    }

    private static Question aQuestion() {
        Question question = new Question(1, "1.1", NistFunction.IDENTIFY, ImplementationGroup.IG1);
        question.replaceTranslations(Map.of("fr", "texte"));
        return question;
    }

    @Test
    void upsert_premiereReponse_creeUneNouvelleReponse() {
        AnswerService service = service();
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        Question question = aQuestion();

        when(answerRepository.findByOrganizationIdAndQuestionId(orgId, questionId)).thenReturn(Optional.empty());
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = service.upsert(orgId, questionId, AnswerValue.YES);

        assertEquals(AnswerValue.YES, response.value());
    }

    @Test
    void upsert_questionInexistante_leveQuestionNotFoundException() {
        AnswerService service = service();
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        when(answerRepository.findByOrganizationIdAndQuestionId(orgId, questionId)).thenReturn(Optional.empty());
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(QuestionNotFoundException.class, () -> service.upsert(orgId, questionId, AnswerValue.YES));
    }

    @Test
    void upsert_reponseDejaExistante_metAJourEnPlaceSansRecreer() {
        AnswerService service = service();
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        Question question = aQuestion();
        Answer existing = new Answer(orgId, question, AnswerValue.NO);

        when(answerRepository.findByOrganizationIdAndQuestionId(orgId, questionId)).thenReturn(Optional.of(existing));
        when(answerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = service.upsert(orgId, questionId, AnswerValue.YES);

        assertEquals(AnswerValue.YES, response.value());
    }

    @Test
    void listForOrganizationPaged_delegueAuRepositoryEtConvertitLaPage() {
        AnswerService service = service();
        UUID orgId = UUID.randomUUID();
        Answer answer = new Answer(orgId, aQuestion(), AnswerValue.YES);

        when(answerRepository.findByOrganizationId(orgId, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(answer), PageRequest.of(0, 2), 5));

        PageResponse<AnswerResponse> response = service.listForOrganizationPaged(orgId, 0, 2);

        assertEquals(0, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.content().size());
    }

    @Test
    void getOne_aucuneReponse_leveAnswerNotFoundException() {
        AnswerService service = service();
        UUID orgId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        when(answerRepository.findByOrganizationIdAndQuestionId(orgId, questionId)).thenReturn(Optional.empty());

        assertThrows(AnswerNotFoundException.class, () -> service.getOne(orgId, questionId));
    }
}
