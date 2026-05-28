package no.nav.pensjon.simulator.trygdetid

import java.time.LocalDate

/**
 * Spesifikasjon (parametre) for beregning av trygdetid når utenlandsopphold er angitt som antall år.
 */
data class TrygdetidsgrunnlagAarsbasertSpec(
    val antallAarUtenlands: Int,
    val tom: LocalDate?,
    val erFoerstegangsberegning: Boolean,
    val foersteUttakDato: LocalDate
)