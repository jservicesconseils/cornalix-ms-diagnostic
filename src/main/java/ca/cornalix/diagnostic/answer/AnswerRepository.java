package ca.cornalix.diagnostic.answer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    Optional<Answer> findByOrganizationIdAndQuestionId(UUID organizationId, UUID questionId);

    List<Answer> findByOrganizationId(UUID organizationId);

    Page<Answer> findByOrganizationId(UUID organizationId, Pageable pageable);

    boolean existsByQuestionId(UUID questionId);
}
