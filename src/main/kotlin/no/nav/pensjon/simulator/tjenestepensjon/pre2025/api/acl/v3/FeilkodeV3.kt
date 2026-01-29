package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode

enum class FeilkodeV3(private val internalValue: Feilkode? = null) {
    TEKNISK_FEIL(internalValue = Feilkode.TEKNISK_FEIL),
    BEREGNING_GIR_NULL_UTBETALING(internalValue = Feilkode.BEREGNING_GIR_NULL_UTBETALING),
    OPPFYLLER_IKKE_INNGANGSVILKAAR(internalValue = Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR),
    BRUKER_IKKE_MEDLEM_AV_TP_ORDNING(internalValue = Feilkode.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING),
    TP_ORDNING_STOETTES_IKKE(internalValue = Feilkode.TP_ORDNING_STOETTES_IKKE);

    companion object {
        fun externalValue(value: Feilkode): FeilkodeV3 =
            entries.single { it.internalValue == value }
    }
}