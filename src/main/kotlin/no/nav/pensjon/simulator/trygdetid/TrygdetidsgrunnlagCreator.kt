package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpphold
import no.nav.pensjon.simulator.trygdetid.InnlandTrygdetidUtil.addInnlandOpphold
import no.nav.pensjon.simulator.trygdetid.TrygdetidTrimmer.removeIkkeAvtaleland
import no.nav.pensjon.simulator.trygdetid.TrygdetidTrimmer.aldersbegrens
import java.time.LocalDate

object TrygdetidsgrunnlagCreator {

    // SimulerFleksibelAPCommand.createTrygdetidsgrunnlagList
    fun trygdetidsperiodeListe(
        utlandOppholdListe: List<TrygdetidOpphold>,
        foedselsdato: LocalDate,
        foersteUttakDato: LocalDate?
    ): List<TTPeriode> {
        // Step 2 Gap-fill domestic basis for pension
        val oppholdListe: List<TrygdetidOpphold> = addInnlandOpphold(utlandOppholdListe, foedselsdato)

        // Step 3 Remove periods of non-contributing countries (ikkeAvtaleLand)
        val opptjeningPeriodeListe: List<TTPeriode> = removeIkkeAvtaleland(oppholdListe)

        // Step 4 Remove periods before age of adulthood and after obtained pension age
        return aldersbegrens(opptjeningPeriodeListe, foersteUttakDato!!, foedselsdato)
    }
}