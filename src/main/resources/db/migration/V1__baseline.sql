-- SCRUM-50 : baseline Flyway -- recree exactement le schema que
-- hibernate.ddl-auto=update a construit jusqu'ici a partir des entites
-- Java (genere via un export Hibernate hors ligne sur ces memes entites,
-- pas ecrit a la main). Contre une base dev/prod deja peuplee, ce script
-- ne s'execute JAMAIS : spring.flyway.baseline-on-migrate=true avec
-- spring.flyway.baseline-version=1 (application-prod.yml) marque la
-- base existante comme deja a la version 1 au premier demarrage, sans
-- rejouer ce CREATE TABLE. Il ne s'execute que sur une base reellement
-- vide (nouvel environnement Postgres, ou H2 local si Flyway y est un
-- jour active).

create table questions (
    id uuid not null,
    cis_control integer not null,
    cis_safeguard varchar(10) not null,
    implementation_group varchar(10) not null check (implementation_group in ('IG1','IG2','IG3')),
    nist_function varchar(20) not null check (nist_function in ('GOVERN','IDENTIFY','PROTECT','DETECT','RESPOND','RECOVER')),
    primary key (id)
);

create table question_translations (
    id uuid not null,
    locale varchar(10) not null,
    text varchar(1000) not null,
    question_id uuid not null,
    primary key (id),
    unique (question_id, locale)
);

create table answers (
    id uuid not null,
    answered_at timestamp(6) with time zone not null,
    organization_id uuid not null,
    answer_value varchar(20) not null check (answer_value in ('YES','NO','PARTIAL','NOT_APPLICABLE')),
    question_id uuid not null,
    primary key (id),
    unique (organization_id, question_id)
);

-- SCRUM-51 : pas de cle etrangere vers answers -- l'audit trail doit
-- survivre meme si la reponse courante est un jour supprimee.
create table answer_history (
    id uuid not null,
    changed_at timestamp(6) with time zone not null,
    changed_by varchar(255) not null,
    new_value varchar(20) not null check (new_value in ('YES','NO','PARTIAL','NOT_APPLICABLE')),
    organization_id uuid not null,
    previous_value varchar(20) check (previous_value in ('YES','NO','PARTIAL','NOT_APPLICABLE')),
    question_id uuid not null,
    primary key (id)
);

alter table if exists answers
    add constraint FK3erw1a3t0r78st8ty27x6v3g1 foreign key (question_id) references questions;

alter table if exists question_translations
    add constraint FK651x0wbgyay2p2ofiaa8mlfcj foreign key (question_id) references questions;
