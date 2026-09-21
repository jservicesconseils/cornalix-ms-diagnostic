package ca.cornalix.diagnostic.question;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Une question du questionnaire de diagnostic, rattachee a un Safeguard
 * CIS Controls v8 et a la fonction NIST CSF 2.0 correspondante. Geree
 * entierement via CRUD (SCRUM-11) -- aucun contenu code en dur dans
 * l'application.
 *
 * Le texte de la question vit dans {@link QuestionTranslation} (une ligne
 * par langue) plutot que directement sur cette entite, pour rester
 * multilingue sans migration de schema a chaque nouvelle langue.
 *
 * Le numero de controle ({@code cisControl}, 1 a 18) est fiable. Le code
 * de Safeguard ({@code cisSafeguard}, ex. "16.4") est une valeur saisie
 * via l'API -- a valider contre le document officiel CIS Controls v8 par
 * la personne qui alimente le catalogue.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

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

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("locale ASC")
    private List<QuestionTranslation> translations = new ArrayList<>();

    protected Question() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Question(int cisControl, String cisSafeguard,
                     NistFunction nistFunction, ImplementationGroup implementationGroup) {
        this.cisControl = cisControl;
        this.cisSafeguard = cisSafeguard;
        this.nistFunction = nistFunction;
        this.implementationGroup = implementationGroup;
    }

    /**
     * Remplace toutes les traductions existantes par celles fournies. Met
     * a jour en place les langues deja presentes plutot que de les
     * supprimer puis les recreer : un clear()+re-ajout avec la meme
     * langue ferait tenter a Hibernate un INSERT avant le DELETE de
     * l'ancienne ligne dans le meme flush, ce qui viole la contrainte
     * unique (question_id, locale).
     */
    public void replaceTranslations(Map<String, String> textByLocale) {
        translations.removeIf(t -> !textByLocale.containsKey(t.getLocale()));

        Set<String> existingLocales = new HashSet<>();
        for (QuestionTranslation translation : translations) {
            existingLocales.add(translation.getLocale());
            translation.setText(textByLocale.get(translation.getLocale()));
        }

        textByLocale.forEach((locale, text) -> {
            if (!existingLocales.contains(locale)) {
                translations.add(new QuestionTranslation(this, locale, text));
            }
        });
    }

    public UUID getId() {
        return id;
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

    public void setCisControl(int cisControl) {
        this.cisControl = cisControl;
    }

    public void setCisSafeguard(String cisSafeguard) {
        this.cisSafeguard = cisSafeguard;
    }

    public void setNistFunction(NistFunction nistFunction) {
        this.nistFunction = nistFunction;
    }

    public void setImplementationGroup(ImplementationGroup implementationGroup) {
        this.implementationGroup = implementationGroup;
    }

    public List<QuestionTranslation> getTranslations() {
        return translations;
    }
}
