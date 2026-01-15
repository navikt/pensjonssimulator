package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidsgrunnlagCreator.trygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag
import java.time.LocalDate

/**
 * NB: Denne brukes bare i forbindelse med utenlandsopphold.
 */
object Kapittel20TrygdetidsgrunnlagCreator {

    // SimulerFleksibelAPCommand.setTrygdetidKap20
    fun kapittel20TrygdetidsperiodeListe(
        utlandPeriodeListe: MutableList<UtlandPeriode>,
        foedselsdato: LocalDate,
        foersteUttakDato: LocalDate?
    ): List<TTPeriode> =
        trygdetidsperiodeListe(
            utenlandsoppholdListe = utlandTrygdetidsgrunnlag(utlandPeriodeListe),
            foedselsdato,
            foersteUttakDato!!
        )
}
