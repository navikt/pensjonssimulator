package no.nav.pensjon.simulator.uttak

import java.time.LocalDate

data class TidligstMuligUttak(
    val uttakDato: LocalDate? = null,
    val uttakGrad: UttakGrad,
    val feil: TidligstMuligUttakFeil? = null
)

data class TidligstMuligUttakFeil(
    val type: TidligstMuligUttakFeilType,
    val beskrivelse: String
)

enum class TidligstMuligUttakFeilType {
    NONE,
    FOR_LAV_OPPTJENING,
    TEKNISK_FEIL
}
