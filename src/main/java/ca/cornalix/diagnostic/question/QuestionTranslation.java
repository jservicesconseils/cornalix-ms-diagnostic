package ca.cornalix.diagnostic.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * Le texte d'une {@link Question} dans une langue donnee. {@code locale}
 * est un code libre (ex. "fr", "en") plutot qu'un enum Java : ajouter une
 * langue plus tard ne demande donc aucun deploiement de code, juste de
 * nouvelles lignes via l'API (SCRUM-11).
 */
@Entity
@Table(name = "question_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "locale"}))
public class QuestionTranslation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(nullable = false, length = 1000)
    private String text;

    protected QuestionTranslation() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public QuestionTranslation(Question question, String locale, String text) {
        this.question = question;
        this.locale = locale;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public String getLocale() {
        return locale;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
