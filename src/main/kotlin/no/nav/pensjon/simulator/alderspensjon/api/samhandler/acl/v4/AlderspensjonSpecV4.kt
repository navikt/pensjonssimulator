package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4

import no.nav.pensjon.simulator.person.Pid.Companion.redact

/**
 * Version 4 of the data transfer object representing a specification for the 'simuler alderspensjon' service.
 */
data class AlderspensjonSpecV4(
    val personId: String? = null,
    val gradertUttak: GradertUttakSpecV4? = null,
    val heltUttakFraOgMedDato: String? = null,
    val aarIUtlandetEtter16: Int? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val fremtidigInntektListe: List<PensjonInntektSpecV4>? = null,
    val rettTilAfpOffentligDato: String? = null
) {
    /**
     * toString with redacted person ID
     */
    override fun toString() =
        "personId: ${redact(personId)}, " +
                "gradertUttak: $gradertUttak, " +
                "heltUttakFraOgMedDato: $heltUttakFraOgMedDato, " +
                "aarIUtlandetEtter16: $aarIUtlandetEtter16, " +
                "epsPensjon: $epsPensjon, " +
                "eps2G: $eps2G, " +
                "fremtidigInntektListe: $fremtidigInntektListe, " +
                "rettTilAfpOffentligDato: $rettTilAfpOffentligDato"
}

data class GradertUttakSpecV4(
    val fraOgMedDato: String? = null,
    val uttaksgrad: Int? = null
)

data class PensjonInntektSpecV4(
    val aarligInntekt: Int? = null,
    val fraOgMedDato: String? = null
)
