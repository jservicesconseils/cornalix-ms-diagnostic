package ca.cornalix.diagnostic.answer;

import ca.cornalix.diagnostic.answer.dto.AnswerResponse;
import ca.cornalix.diagnostic.question.Question;
import ca.cornalix.diagnostic.question.QuestionNotFoundException;
import ca.cornalix.diagnostic.question.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SCRUM-12 : une seule reponse courante par organisation x question --
 * repondre a nouveau met a jour la reponse existante (upsert), pas
 * d'historique.
 */
@Service
@Transactional
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    public AnswerResponse upsert(UUID organizationId, UUID questionId, AnswerValue value) {
        Answer answer = answerRepository.findByOrganizationIdAndQuestionId(organizationId, questionId)
                .map(existing -> {
                    existing.updateValue(value);
                    return existing;
                })
                .orElseGet(() -> {
                    Question question = questionRepository.findById(questionId)
                            .orElseThrow(() -> new QuestionNotFoundException(questionId));
                    return new Answer(organizationId, question, value);
                });

        return toResponse(answerRepository.save(answer));
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

    private AnswerResponse toResponse(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getValue(),
                answer.getAnsweredAt()
        );
    }
}
