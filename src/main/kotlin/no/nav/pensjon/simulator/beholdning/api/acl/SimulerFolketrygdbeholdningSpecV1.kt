package no.nav.pensjon.simulator.beholdning.api.acl

data class SimulerFolketrygdbeholdningSpecV1(
    val personId: String? = null,
    val uttaksdato: String? = null,
    val fremtidigInntektListe: List<FolketrygdbeholdningInntektSpecV1>? = null,
    val arIUtlandetEtter16: Int? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null
)

data class FolketrygdbeholdningInntektSpecV1(
    val arligInntekt: Int,
    val fraOgMedDato: String
)
