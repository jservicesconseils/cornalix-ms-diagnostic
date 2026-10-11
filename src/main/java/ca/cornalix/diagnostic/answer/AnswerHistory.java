package ca.cornalix.diagnostic.answer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Une entree de l'historique des reponses (SCRUM-51) : enregistree a
 * chaque upsert (creation ET modification), juste avant d'ecraser la
 * valeur courante sur {@link Answer}. {@code previousValue} est nul pour
 * la toute premiere reponse a une question (rien a journaliser avant).
 *
 * Ni {@code organizationId} ni {@code questionId} ne sont des cles
 * etrangeres vers Answer -- l'historique doit survivre meme si la
 * reponse courante est un jour supprimee (pas le cas aujourd'hui, mais
 * on ne veut pas qu'un futur DELETE sur answers emporte l'audit trail
 * avec lui).
 */
@Entity
@Table(name = "answer_history")
public class AnswerHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_value", length = 20)
    private AnswerValue previousValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_value", nullable = false, length = 20)
    private AnswerValue newValue;

    // sub du jeton Cognito de l'auteur du changement -- pas une cle
    // etrangere vers app_users (cornalix-ms-identity, base separee,
    // SCRUM-7/8/9).
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected AnswerHistory() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public AnswerHistory(UUID organizationId, UUID questionId, AnswerValue previousValue, AnswerValue newValue,
                          String changedBy) {
        this.organizationId = organizationId;
        this.questionId = questionId;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public AnswerValue getPreviousValue() {
        return previousValue;
    }

    public AnswerValue getNewValue() {
        return newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
