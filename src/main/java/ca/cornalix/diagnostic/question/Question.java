package ca.cornalix.diagnostic.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Une question du questionnaire de diagnostic, rattachee a un Safeguard
 * CIS Controls v8 et a la fonction NIST CSF 2.0 correspondante.
 *
 * Le numero de controle ({@code cisControl}, 1 a 18) est fiable. Le code
 * de Safeguard ({@code cisSafeguard}, ex. "16.4") seede par
 * QuestionCatalogSeeder est une valeur illustrative -- a valider contre le
 * document officiel CIS Controls v8 avant toute utilisation face a un
 * client reel (voir SCRUM-10).
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "cis_control", nullable = false)
    private int cisControl;

    @Column(name = "cis_safeguard", nullable = false, length = 10)
    private String cisSafeguard;

    @Enumerated(EnumType.STRING)
    @Column(name = "nist_function", nullable = false, length = 20)
    private NistFunction nistFunction;

    @Enumerated(EnumType.STRING)
    @Column(name = "implementation_group", nullable = false, length = 10)
    private ImplementationGroup implementationGroup;

    protected Question() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Question(String text, int cisControl, String cisSafeguard,
                     NistFunction nistFunction, ImplementationGroup implementationGroup) {
        this.text = text;
        this.cisControl = cisControl;
        this.cisSafeguard = cisSafeguard;
        this.nistFunction = nistFunction;
        this.implementationGroup = implementationGroup;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getCisControl() {
        return cisControl;
    }

    public String getCisSafeguard() {
        return cisSafeguard;
    }

    public NistFunction getNistFunction() {
        return nistFunction;
    }

    public ImplementationGroup getImplementationGroup() {
        return implementationGroup;
    }
}
