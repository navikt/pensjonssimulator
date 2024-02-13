package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.uttak.TidligstMuligUttakFeilType

enum class TidligstMuligUttakFeilTypeV1(val internalValue: TidligstMuligUttakFeilType) {

    NONE(TidligstMuligUttakFeilType.NONE),
    FOR_LAV_OPPTJENING(TidligstMuligUttakFeilType.FOR_LAV_OPPTJENING),
    TEKNISK_FEIL(TidligstMuligUttakFeilType.TEKNISK_FEIL);

    companion object {
        private val values = entries.toTypedArray()

        fun fromInternalValue(value: TidligstMuligUttakFeilType) = values.first { it.internalValue == value }
    }
}
