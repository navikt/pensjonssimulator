package no.nav.pensjon.simulator.tech.representasjon

/**
 * Simulatoren bruker data for en gitt person, identifisert med PID (person-ID).
 * Denne enum brukes for å holde styr på personens rolle i forskjellige representasjonsforhold.
 */
enum class RepresentertRolle {
    NONE,
    SELV, // personen er den samme som den som er innlogget
    UNDER_VEILEDNING // personen undergår veiledning; den innloggede er veileder
}