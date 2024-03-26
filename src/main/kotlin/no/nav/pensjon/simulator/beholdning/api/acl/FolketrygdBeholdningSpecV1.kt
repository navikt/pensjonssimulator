package no.nav.pensjon.simulator.beholdning.api.acl

/**
 * Version 1 of specification for 'simuler folketrygdbeholdning'.
 */
data class FolketrygdBeholdningSpecV1(
    val personId: String = "",
    val uttaksdato: String = "",
    val fremtidigInntektListe: List<BeholdningInntektSpecV1>? = null,
    val arIUtlandetEtter16: Int? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null
)

data class BeholdningInntektSpecV1(
    val arligInntekt: Int? = null,
    val fraOgMedDato: String = ""
)
