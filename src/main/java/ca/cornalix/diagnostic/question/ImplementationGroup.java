package ca.cornalix.diagnostic.question;

/**
 * Groupe d'implementation CIS Controls v8 minimal requis pour qu'une
 * question s'applique : IG1 (hygiene de base, toutes les organisations),
 * IG2 (organisations avec plus de ressources/risque), IG3 (organisations
 * face a des menaces sophistiquees). Cumulatif dans la doctrine CIS : une
 * organisation IG3 implemente aussi les questions IG1 et IG2.
 *
 * SCRUM-10 ne filtre pas encore par ce groupe (voir portee du recit) --
 * il est neanmoins capture des maintenant pour ne pas avoir a retoucher
 * le modele de donnees quand le filtrage par tranche d'employes de
 * l'organisation sera ajoute.
 */
public enum ImplementationGroup {
    IG1,
    IG2,
    IG3
}
