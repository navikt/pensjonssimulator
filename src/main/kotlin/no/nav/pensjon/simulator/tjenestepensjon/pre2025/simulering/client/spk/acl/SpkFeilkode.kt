package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode

/**
 * Definerer mapping mellom feilkoder og feilmeldinger returnert av SPK og feilkoder brukt i internt domene.
 */
enum class SpkFeilkode(
    private val externalValues: List<String> = emptyList(),
    private val externalErrorMessages: List<String> = emptyList(),
    private val internalValue: Feilkode
) {
    TEKNISK_FEIL(
        externalValues = listOf("CALC001", "CALC003", "CALC004", "CALC005"),
        internalValue = Feilkode.TEKNISK_FEIL
    ),
    BEREGNING_GIR_NULL_UTBETALING(
        externalValues = listOf("CALC002"),
        externalErrorMessages = listOf("Beregning gir 0 i utbetaling.").map { "Validation problem: $it" },
        internalValue = Feilkode.BEREGNING_GIR_NULL_UTBETALING
    ),
    OPPFYLLER_IKKE_INNGANGSVILKAAR(
        externalValues = listOf("CALC002"),
        externalErrorMessages = listOf(
            "Beregning gir 0 i utbetaling.",
            "Delvis AFP ikke lovlig med reststilling under 60%.",
            "Delvis AFP ikke lovlig med nedgang under 10%.",
            "Tjenestetid mindre enn 3 år."
        ).map { "Validation problem: $it" },
        internalValue = Feilkode.OPPFYLLER_IKKE_INNGANGSVILKAAR
    ),
    BRUKER_IKKE_MEDLEM_AV_TP_ORDNING(
        internalValue = Feilkode.BRUKER_IKKE_MEDLEM_AV_TP_ORDNING
    ),
    TP_ORDNING_STOETTES_IKKE(
        internalValue = Feilkode.TP_ORDNING_STOETTES_IKKE
    );

    companion object {
        fun internalValue(externalValue: String, externalErrorMessage: String): Feilkode =
            fromExternalValue(externalValue, externalErrorMessage).internalValue

        private fun fromExternalValue(externalValue: String, externalErrorMessage: String): SpkFeilkode =
            entries.firstOrNull {
                if (externalValue == "CALC002")
                    it.externalValues.contains(externalValue) && it.externalErrorMessages.contains(externalErrorMessage)
                else
                    it.externalValues.contains(externalValue)
            } ?: TEKNISK_FEIL
    }
}