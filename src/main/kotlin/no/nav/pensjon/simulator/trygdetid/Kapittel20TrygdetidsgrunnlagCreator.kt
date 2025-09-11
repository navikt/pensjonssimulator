package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.trygdetid.TrygdetidsgrunnlagCreator.trygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag
import java.time.LocalDate

object Kapittel20TrygdetidsgrunnlagCreator {

    // SimulerFleksibelAPCommand.setTrygdetidKap20
    fun kapittel20TrygdetidperiodeListe(spec: SimuleringSpec, foedselsdato: LocalDate): List<TTPeriode> {
        val trygdetidGrunnlagUtlandOppholdListe = utlandTrygdetidsgrunnlag(spec.utlandPeriodeListe)

        val trygdetidGrunnlagListe = trygdetidsperiodeListe(
            trygdetidGrunnlagUtlandOppholdListe,
            foedselsdato,
            spec.foersteUttakDato
        )

        return trygdetidGrunnlagListe
    }
}
