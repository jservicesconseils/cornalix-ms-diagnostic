package ca.cornalix.diagnostic.answer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, UUID> {

    List<AnswerHistory> findByOrganizationIdAndQuestionIdOrderByChangedAtDesc(UUID organizationId, UUID questionId);
}
