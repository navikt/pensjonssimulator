package no.nav.pensjon.simulator.trygdetid

/**
 * Spesifikasjon (parametre) for beregning av trygdetid når personen er anonym.
 */
data class TrygdetidsgrunnlagAnonymSpec(
    val antallAarUtenlands: Int,
    val foedselsaar: Int
)