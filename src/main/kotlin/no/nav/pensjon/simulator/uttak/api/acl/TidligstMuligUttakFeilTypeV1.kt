package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.uttak.TidligstMuligUttakFeilType

enum class TidligstMuligUttakFeilTypeV1(val internalValue: TidligstMuligUttakFeilType) {

    NONE(TidligstMuligUttakFeilType.NONE),
    FOR_LAV_OPPTJENING(TidligstMuligUttakFeilType.FOR_LAV_OPPTJENING),
    TEKNISK_FEIL(TidligstMuligUttakFeilType.TEKNISK_FEIL);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromInternalValue(value: TidligstMuligUttakFeilType) = entries.first { it.internalValue == value }
    }
}
