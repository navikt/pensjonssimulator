package no.nav.pensjon.simulator.alderspensjon.api.acl

/**
 * Version 4 of specification for 'simuler alderspensjon'.
 * NB: Versions 1 to 3 are services offered by PEN.
 */
data class AlderspensjonSpecV4(
    val personId: String? = null,
    val gradertUttak: GradertUttakV4? = null,
    val heltUttakFraOgMedDato: String? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val arIUtlandetEtter16: Int? = null,
    val fremtidigInntektListe: List<PensjonInntektSpecV4>? = null,
    val rettTilAfpOffentligDato: String? = null
)

data class GradertUttakV4(
    val fraOgMedDato: String? = null,
    val uttaksgrad: Int? = null
)

data class PensjonInntektSpecV4(
    val arligInntekt: Int? = null,
    val fraOgMedDato: String? = null
)
