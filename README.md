# pensjonssimulator

Backend-applikasjon for simulering av alderspensjon og AFP (avtalefestet pensjon).

Simulering innebærer å beregne en prognose for pensjonsutbetaling.

Applikasjonen eksponerer API-er som brukes av Nav internt og av tjenestepensjonsordninger. 

## Teknologi

* [Java 21](https://openjdk.org/projects/jdk/21/)
* [Kotlin](https://kotlinlang.org/)
* [Spring Boot 3](https://spring.io/projects/spring-boot)
* [Maven](https://maven.apache.org/)
* [NAIS](https://nais.io/) (med oppsett for [Maskinporten](https://www.digdir.no/felleslosninger/maskinporten/869))

## Dokumentasjon

* [Eksterne API-er](https://navikt.github.io/pensjon-ekstern-api/api/alderspensjon/simulering2025/simulering2025.html)

* [Nav-intern dokumentasjon](https://confluence.adeo.no/display/PEN/Pensjonssimulator-app)

## Henvendelser

Nav-interne henvendelser kan sendes via Slack i kanalen [#pensjonskalkulator](https://nav-it.slack.com/archives/C04M46SPSRL).
