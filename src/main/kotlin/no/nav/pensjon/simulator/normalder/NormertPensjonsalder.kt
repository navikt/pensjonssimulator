package no.nav.pensjon.simulator.normalder

import no.nav.pensjon.simulator.alder.Alder

data class NormertPensjonsalder(
    val aarskull: Int,
    val alder: Alder,
    val nedreAlder: Alder,
    val oevreAlder: Alder,
    val type: PensjonsalderType
)

enum class PensjonsalderType {
    FAST,
    PROGNOSE
}
