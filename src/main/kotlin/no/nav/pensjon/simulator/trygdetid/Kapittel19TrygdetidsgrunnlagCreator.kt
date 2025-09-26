package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.trygdetidPeriode
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpphold
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidsgrunnlagCreator.trygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag
import java.time.LocalDate

/**
 * NB: Denne brukes bare i forbindelse med utenlandsopphold.
 */
object Kapittel19TrygdetidsgrunnlagCreator {

    // SimulerFleksibelAPCommand.setTrygetidKap19
    fun kapittel19TrygdetidsperiodeListe(
        opptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>,
        utlandPeriodeListe: MutableList<UtlandPeriode>,
        foedselsdato: LocalDate,
        foersteUttakDato: LocalDate?
    ): List<TTPeriode> {
        val trygdetidsgrunnlagMedPensjonspoengListe = mapOpptjeningToTrygdetid(opptjeningsgrunnlagListe)

        val utlandTrygdetidsgrunnlag =
            if (trygdetidsgrunnlagMedPensjonspoengListe.isEmpty())
                utlandTrygdetidsgrunnlag(utlandPeriodeListe)
            else
                utlandTrygdetidsgrunnlag(utlandPeriodeListe, trygdetidsgrunnlagMedPensjonspoengListe)

        val trygdetidsgrunnlagListe = trygdetidsperiodeListe(
            utenlandsoppholdListe = utlandTrygdetidsgrunnlag,
            foedselsdato,
            foersteUttakDato!!
        )

        return trygdetidsgrunnlagListe
    }

    private fun mapOpptjeningToTrygdetid(opptjeningListe: List<Opptjeningsgrunnlag>): List<TrygdetidOpphold> {
        val trygdetidListe: MutableList<TrygdetidOpphold> = mutableListOf()
        val addedAarListe: MutableSet<Int> = HashSet()

        for (opptjening in opptjeningListe) {
            val aar = opptjening.ar
            if (addedAarListe.contains(aar) || opptjening.pp <= 0) continue

            val trygdetidGrunnlag = trygdetidPeriode(
                fom = LocalDate.of(aar, 1, 1),
                tom = LocalDate.of(aar, 12, 31),
                land = LandkodeEnum.NOR
            )

            trygdetidListe.add(TrygdetidOpphold(periode = trygdetidGrunnlag, arbeidet = true))
            addedAarListe.add(aar)
        }

        return trygdetidListe
    }
}