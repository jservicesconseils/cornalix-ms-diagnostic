cornalix-ms-diagnostic — service de diagnostic cybersécurité

Rôle dans l'architecture
Premier microservice **métier** de Cornalix (voir la proposition d'architecture, §5 : cycle de valeur). Il gère le questionnaire de diagnostic d'une organisation, structuré selon les 6 fonctions du NIST CSF 2.0 (Govern, Identify, Protect, Detect, Respond, Recover) et les contrôles CIS Controls v8 (groupe IG1/IG2/IG3 selon la tranche d'employés de l'organisation). Il ne gère ni l'authentification ni les organisations elles-mêmes — ça, c'est `cornalix-ms-identity`, dont il consomme les jetons.

Ce qu'il fait (dans son périmètre, MVP)

Enregistrer les réponses d'une organisation au questionnaire de diagnostic, rattachées à son `tenant_id`.
Exposer les réponses complétées à `cornalix-ms-scoring` (appel HTTP en Phase 1 — voir décision d'architecture sur SCRUM-7/8/9 : microservices séparés dès le MVP, bus d'événements Kafka/MSK reporté à la Phase 2).

Ce qu'il ne fait PAS (hors périmètre, volontairement)

Calculer le score de maturité — ça, c'est `cornalix-ms-scoring` (SCRUM-8).
Générer des recommandations ou un plan d'action — services séparés, Phase 2.
Gérer l'authentification, les organisations ou les utilisateurs — `cornalix-ms-identity`.

Sécurité

Comme tous les microservices Cornalix, ce service ne gère aucun mot de passe : il valide les jetons JWT émis par le même User Pool Amazon Cognito que `cornalix-ms-identity` (`SecurityConfig`, JWKS). Toute route (hors `/actuator/health` et `/actuator/info`) exige un jeton valide, et chaque accès aux données d'une organisation doit être vérifié contre le `tenant_id`/`tenant_scope` du jeton — même garde-fou que `/api/v1/organizations/{id}` sur `cornalix-ms-identity` (voir SCRUM-6).

Suivi Jira

Epic : [SCRUM-7 — Cornalix — Diagnostic (MVP)](https://jservicesconseils.atlassian.net/browse/SCRUM-7), projet SCRUM.

Développer en local

```
mvn spring-boot:run
```

Démarre sur `http://localhost:8082` (8080 = core-api, 8081 = `cornalix-ms-identity`). Base H2 en mémoire, aucune installation requise. Nécessite un jeton Cognito valide (même pool que `cornalix-ms-identity`) pour toute route protégée.
