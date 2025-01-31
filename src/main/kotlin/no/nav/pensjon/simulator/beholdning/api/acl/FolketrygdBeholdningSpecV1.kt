package no.nav.pensjon.simulator.beholdning.api.acl

import no.nav.pensjon.simulator.person.Pid.Companion.redact

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
) {
    override fun toString(): String =
        "personId: ${redact(personId)}, " +
                "uttaksdato: $uttaksdato, " +
                "fremtidigInntektListe: $fremtidigInntektListe, " +
                "arIUtlandetEtter16: $arIUtlandetEtter16, " +
                "epsPensjon: $epsPensjon, " +
                "eps2G: $eps2G"
}

data class BeholdningInntektSpecV1(
    val arligInntekt: Int? = null,
    val fraOgMedDato: String = ""
)
