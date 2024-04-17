package no.nav.pensjon.simulator.alderspensjon.api.acl

/**
 * Version 1 of specification for 'simuler alderspensjon'.
 */
data class AlderspensjonSpecV1(
    val personId: String = "",
    val gradertUttak: GradertUttakV1? = null,
    val heltUttakFraOgMedDato: String = "",
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val arIUtlandetEtter16: Int? = null,
    val fremtidigInntektListe: List<PensjonInntektSpecV1>? = null,
    val rettTilAfpOffentligDato: String = ""
)

data class GradertUttakV1(
    val fraOgMedDato: String = "",
    val uttaksgrad: Int? = null
)

data class PensjonInntektSpecV1(
    val arligInntekt: Int? = null,
    val fraOgMedDato: String = ""
)
