package ca.cornalix.diagnostic.question;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static ca.cornalix.diagnostic.question.ImplementationGroup.IG1;
import static ca.cornalix.diagnostic.question.ImplementationGroup.IG2;
import static ca.cornalix.diagnostic.question.NistFunction.DETECT;
import static ca.cornalix.diagnostic.question.NistFunction.IDENTIFY;
import static ca.cornalix.diagnostic.question.NistFunction.PROTECT;

/**
 * Seed un sous-ensemble representatif du catalogue au demarrage (SCRUM-10) :
 * pas les 18 controles CIS au complet, juste assez pour couvrir les 4
 * domaines identifies (reseau, logiciels, appareils mobiles, CI/CD) et
 * valider le modele de bout en bout. Ne seede que si la table est vide,
 * pour rester idempotent entre redemarrages.
 */
@Component
public class QuestionCatalogSeeder implements ApplicationRunner {

    private final QuestionRepository repository;

    public QuestionCatalogSeeder(QuestionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(
                // Reseau -- CIS Controls 12 (gestion) / 13 (surveillance)
                new Question(
                        "Disposez-vous d'un inventaire a jour de tous les equipements reseau actifs "
                                + "(routeurs, commutateurs, pare-feu, points d'acces) ?",
                        12, "12.1", IDENTIFY, IG1),
                new Question(
                        "Le reseau est-il segmente pour isoler les systemes critiques ou sensibles "
                                + "du reste du trafic ?",
                        12, "12.2", PROTECT, IG2),
                new Question(
                        "Le trafic reseau est-il surveille pour detecter des activites suspectes "
                                + "ou anormales ?",
                        13, "13.1", DETECT, IG2),

                // Logiciels -- CIS Controls 2 (inventaire) / 7 (vulnerabilites)
                new Question(
                        "Disposez-vous d'un inventaire de tous les logiciels autorises utilises "
                                + "dans l'organisation ?",
                        2, "2.1", IDENTIFY, IG1),
                new Question(
                        "Les logiciels non autorises ou non pris en charge sont-ils identifies "
                                + "puis supprimes ou bloques ?",
                        2, "2.5", PROTECT, IG2),
                new Question(
                        "Les correctifs de securite sont-ils appliques aux logiciels dans un delai "
                                + "defini apres leur publication ?",
                        7, "7.3", PROTECT, IG1),

                // Appareils mobiles -- CIS Controls 1 (inventaire) / 4 (configuration)
                new Question(
                        "Disposez-vous d'un inventaire de tous les appareils mobiles utilises pour "
                                + "acceder aux donnees de l'entreprise ?",
                        1, "1.1", IDENTIFY, IG1),
                new Question(
                        "Les appareils mobiles de l'entreprise sont-ils geres par une solution de "
                                + "gestion des appareils mobiles (MDM), incluant verrouillage par mot "
                                + "de passe et effacement a distance ?",
                        4, "4.11", PROTECT, IG2),

                // Deploiement applicatif / CI-CD -- CIS Control 16
                new Question(
                        "Le code source des applications developpees en interne fait-il l'objet "
                                + "d'une revue avant sa mise en production ?",
                        16, "16.7", PROTECT, IG2),
                new Question(
                        "L'acces au pipeline de deploiement (CI/CD) est-il restreint aux personnes "
                                + "autorisees et les actions y sont-elles journalisees ?",
                        16, "16.1", PROTECT, IG2)
        ));
    }
}
