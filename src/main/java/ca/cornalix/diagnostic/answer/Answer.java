package ca.cornalix.diagnostic.answer;

import ca.cornalix.diagnostic.question.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * La reponse courante d'une organisation (tenant) a une question du
 * questionnaire. Une seule reponse par organisation x question --
 * repondre a nouveau remplace la reponse precedente (voir SCRUM-12,
 * pas d'historique dans ce recit).
 *
 * {@code organizationId} n'est pas une cle etrangere -- c'est l'id de
 * l'Organization sur cornalix-ms-identity (base separee, SCRUM-7/8/9),
 * verifie contre le tenant_id/tenant_scope de l'appelant au niveau du
 * controleur, jamais fourni tel quel par le client.
 */
@Entity
@Table(name = "answers", uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "question_id"}))
public class Answer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // "value" est un mot reserve en SQL H2 -- colonne renommee pour eviter
    // l'echec silencieux de la creation de table (ddl-auto: update logge
    // l'erreur mais ne fait pas echouer le demarrage, a repere via test
    // manuel).
    @Enumerated(EnumType.STRING)
    @Column(name = "answer_value", nullable = false, length = 20)
    private AnswerValue value;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    protected Answer() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Answer(UUID organizationId, Question question, AnswerValue value) {
        this.organizationId = organizationId;
        this.question = question;
        this.value = value;
        this.answeredAt = Instant.now();
    }

    public void updateValue(AnswerValue value) {
        this.value = value;
        this.answeredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public Question getQuestion() {
        return question;
    }

    public AnswerValue getValue() {
        return value;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }
}
