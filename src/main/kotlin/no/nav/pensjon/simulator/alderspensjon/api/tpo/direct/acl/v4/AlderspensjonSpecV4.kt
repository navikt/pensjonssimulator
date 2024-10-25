package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

/**
 * Version 4 of specification for 'simuler alderspensjon'.
 * NB: Versions 1 to 3 are services offered by PEN.
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
)

data class GradertUttakSpecV4(
    val fraOgMedDato: String? = null,
    val uttaksgrad: Int? = null
)

data class PensjonInntektSpecV4(
    val aarligInntekt: Int? = null,
    val fraOgMedDato: String? = null
)
