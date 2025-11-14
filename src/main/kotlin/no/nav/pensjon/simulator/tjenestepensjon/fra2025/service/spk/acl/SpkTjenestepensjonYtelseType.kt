package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl

import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType

enum class SpkTjenestepensjonYtelseType(
    val externalValue: String,
    val internalValue: TjenestepensjonYtelseType
) {
    ALDERSPENSJON_OPPTJENT_FOER_2020(
        externalValue = "APOF2020",
        internalValue = TjenestepensjonYtelseType.ALDERSPENSJON_OPPTJENT_FOER_2020
    ),
    BETINGET_TJENESTEPENSJON(
        externalValue = "BTP",
        internalValue = TjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON
    ),
    OFFENTLIG_AFP(
        externalValue = "OAFP",
        internalValue = TjenestepensjonYtelseType.OFFENTLIG_AFP
    ),
    OVERGANGSTILLEGG(
        externalValue = "OT6370",
        internalValue = TjenestepensjonYtelseType.OVERGANGSTILLEGG
    ),
    PAASLAG(
        externalValue = "PAASLAG",
        internalValue = TjenestepensjonYtelseType.PAASLAG
    ),
    SAERALDERSPAASLAG(
        externalValue = "SAERALDERSPAASLAG",
        internalValue = TjenestepensjonYtelseType.SAERALDERSPAASLAG
    );

    companion object {
        fun alleUnntatt(vararg typer: SpkTjenestepensjonYtelseType): List<SpkTjenestepensjonYtelseType> =
            SpkTjenestepensjonYtelseType.entries.filter { it !in typer }

        fun internalValue(externalValue: String?): TjenestepensjonYtelseType? =
            entries.singleOrNull {
                it.externalValue.equals(other = externalValue, ignoreCase = true)
            }?.internalValue
    }

}
