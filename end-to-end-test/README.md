# End-to-end tester

Applikasjonen kjører end-to-end tester mot **Pensjonssimulator** i dev-miljø.

## Nye tester
1. Legg til JSON-filer for request og forventet response i `src/main/resources/`-mappen.
2. Legg til en ny `Resource` i `IntegrasjonstestApplication.kt` med URI-path fra **Pensjonssimulator**
3. Merge ny test til **main** eller **sandbox**-branch

## Teknologi:
- Kotlin
- Ktor Client - [Ktor Documentation](https://ktor.io/docs/client-create-new-application.html)
- Gradle
- NAIS Job - [NAIS Documentation](https://doc.nav.cloud.nais.io/workloads/job/)

## Bygg og kjøring
| Task                          | Description             |
|-------------------------------|-------------------------|
| `./gradlew test`              | Kjører tester           |
| `./gradlew build`             | Bygger og kjører tester |

## End-to-End tester i NAIS:
Testen deployes av GitHub-action til NAIS og kjører som NAIS-job etter vellykket deploy av Sandbox-branch til dev-miljø.   
Feilede tester logges med responser fra **Pensjonssimulator** som ikke stemte overens med forventede responser.    
Det logges testresultater, og jobben avsluttes.

```
Test failures: 2, [EvaluationResult(responseIsAsExpected=false, path=/api/v0/simuler-afp-etterfulgt-av-alderspensjon, actualResponse={"aarsakListeIkkeSuksess":[],"alderspensjon...]
The job has been completed. Tests run: 10, failures: 2
```
