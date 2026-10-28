package no.nav.pensjon.simulator.validity

enum class ProblemType(val alvorlighetsgrad: Alvorlighetsgrad = Alvorlighetsgrad.INFORMASJON) {
    // Klientfeil:
    UGYLDIG_UTTAKSDATO,
    UGYLDIG_UTTAKSGRAD,
    UGYLDIG_SIVILSTATUS,
    UGYLDIG_INNTEKT,
    UGYLDIG_ANTALL_AAR,
    UGYLDIG_PERSONIDENT,
    PERSON_IKKE_FUNNET(alvorlighetsgrad = Alvorlighetsgrad.ADVARSEL),
    PERSON_FOR_LAV_ALDER,
    PERSON_FOR_HOEY_ALDER,
    UTILSTREKKELIG_INNTEKT,
    UTILSTREKKELIG_OPPTJENING,
    UTILSTREKKELIG_TRYGDETID,
    ANNEN_KLIENTFEIL(alvorlighetsgrad = Alvorlighetsgrad.ADVARSEL),

    // Klient- eller serverfeil:
    REGELBRUDD(alvorlighetsgrad = Alvorlighetsgrad.FEIL), // feil fra pensjon-regler

    // Serverfeil:
    INTERN_DATA_INKONSISTENS(alvorlighetsgrad = Alvorlighetsgrad.FEIL),
    IMPLEMENTASJONSFEIL(alvorlighetsgrad = Alvorlighetsgrad.FEIL),
    TREDJEPARTSFEIL(alvorlighetsgrad = Alvorlighetsgrad.FEIL),
    ANNEN_SERVERFEIL(alvorlighetsgrad = Alvorlighetsgrad.FEIL)
}