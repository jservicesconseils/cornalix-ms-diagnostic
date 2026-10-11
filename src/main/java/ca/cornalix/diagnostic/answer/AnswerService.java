package ca.cornalix.diagnostic.answer;

import ca.cornalix.diagnostic.answer.dto.AnswerHistoryResponse;
import ca.cornalix.diagnostic.answer.dto.AnswerResponse;
import ca.cornalix.diagnostic.common.PageResponse;
import ca.cornalix.diagnostic.question.Question;
import ca.cornalix.diagnostic.question.QuestionNotFoundException;
import ca.cornalix.diagnostic.question.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SCRUM-12 : une seule reponse courante par organisation x question --
 * repondre a nouveau met a jour la reponse existante (upsert).
 *
 * SCRUM-51 : chaque upsert (creation ET modification) journalise une
 * entree dans answer_history avant d'ecrire la nouvelle valeur -- "pas
 * d'historique" (SCRUM-12) ne voulait dire que "une seule reponse
 * courante exploitable pour le score", pas "aucune trace des changements".
 */
@Service
@Transactional
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerHistoryRepository answerHistoryRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository,
                          AnswerHistoryRepository answerHistoryRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.answerHistoryRepository = answerHistoryRepository;
    }

    public AnswerResponse upsert(UUID organizationId, UUID questionId, AnswerValue value, String changedBy) {
        AnswerValue previousValue = null;

        Answer answer = answerRepository.findByOrganizationIdAndQuestionId(organizationId, questionId).orElse(null);
        if (answer != null) {
            previousValue = answer.getValue();
            answer.updateValue(value);
        } else {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new QuestionNotFoundException(questionId));
            answer = new Answer(organizationId, question, value);
        }

        answerHistoryRepository.save(new AnswerHistory(organizationId, questionId, previousValue, value, changedBy));

        return toResponse(answerRepository.save(answer));
    }

    @Transactional(readOnly = true)
    public List<AnswerHistoryResponse> listHistory(UUID organizationId, UUID questionId) {
        return answerHistoryRepository.findByOrganizationIdAndQuestionIdOrderByChangedAtDesc(organizationId, questionId)
                .stream()
                .map(h -> new AnswerHistoryResponse(h.getId(), h.getPreviousValue(), h.getNewValue(), h.getChangedBy(), h.getChangedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AnswerResponse getOne(UUID organizationId, UUID questionId) {
        Answer answer = answerRepository.findByOrganizationIdAndQuestionId(organizationId, questionId)
                .orElseThrow(() -> new AnswerNotFoundException(organizationId, questionId));
        return toResponse(answer);
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> listForOrganization(UUID organizationId) {
        return answerRepository.findByOrganizationId(organizationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AnswerResponse> listForOrganizationPaged(UUID organizationId, int page, int size) {
        return PageResponse.from(
                answerRepository.findByOrganizationId(organizationId, PageRequest.of(page, size)).map(this::toResponse));
    }

    private AnswerResponse toResponse(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getValue(),
                answer.getAnsweredAt()
        );
    }
}
