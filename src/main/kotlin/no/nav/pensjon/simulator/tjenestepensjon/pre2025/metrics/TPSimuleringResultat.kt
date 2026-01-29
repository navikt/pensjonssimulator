package no.nav.pensjon.simulator.tjenestepensjon.pre2025.metrics

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode

enum class SPKResultatKodePre2025 {
    OK,
    INGEN_UTBETALING,
    OPPFYLLER_IKKE_INNGANGSVILKAAR,
    TEKNISK_FEIL_HOS_SPK,
    INGEN_STILLINGSPROSENT,
    UKJENT_FEIL_HOS_SPK,
    ANNEN_FEIL;

    companion object {
        fun fromFeilkode(feilkode: Feilkode) =
            when (feilkode) {
                Feilkode.BEREGNING_GIR_NULL_UTBETALING -> INGEN_UTBETALING
                Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR -> OPPFYLLER_IKKE_INNGANGSVILKAAR
                Feilkode.TEKNISK_FEIL -> TEKNISK_FEIL_HOS_SPK
                else -> ANNEN_FEIL
            }
    }
}
