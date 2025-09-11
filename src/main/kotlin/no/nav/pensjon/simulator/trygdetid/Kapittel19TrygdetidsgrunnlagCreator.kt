package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.trygdetidPeriode
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpphold
import no.nav.pensjon.simulator.trygdetid.TrygdetidsgrunnlagCreator.trygdetidsperiodeListe
import no.nav.pensjon.simulator.trygdetid.UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag
import java.time.LocalDate

object Kapittel19TrygdetidsgrunnlagCreator {

    // SimulerFleksibelAPCommand.setTrygetidKap19
    fun kapittel19TrygdetidsperiodeListe(
        spec: SimuleringSpec,
        opptjeningsgrunnlagListe: MutableList<Opptjeningsgrunnlag>,
        foedselsdato: LocalDate
    ): List<TTPeriode> {
        val trygdetidsgrunnlagMedPensjonspoengListe = mapOpptjeningToTrygdetid(opptjeningsgrunnlagListe)
        val utlandPeriodeListe = spec.utlandPeriodeListe

        val trygdetidsgrunnlagUtlandOppholdListe =
            if (trygdetidsgrunnlagMedPensjonspoengListe.isEmpty())
                utlandTrygdetidsgrunnlag(utlandPeriodeListe)
            else
                utlandTrygdetidsgrunnlag(utlandPeriodeListe, trygdetidsgrunnlagMedPensjonspoengListe)

        val trygdetidsgrunnlagListe = trygdetidsperiodeListe(
            utlandOppholdListe = trygdetidsgrunnlagUtlandOppholdListe,
            foedselsdato,
            foersteUttakDato = foersteUttakDato(spec)
        )

        return trygdetidsgrunnlagListe
    }

    /**
     * For 'pre-2025 offentlig AFP etterfulgt av alderspensjon' gjelder:
     * - foersteUttakDato = uttak av AFP
     * - heltUttakDato = uttak av alderspensjon
     * Det er alderspensjonsuttaket (og dermed heltUttakDato) som er relevant for trygdetiden her
     */
    private fun foersteUttakDato(spec: SimuleringSpec): LocalDate? =
        if (spec.gjelderPre2025OffentligAfpEtterfulgtAvAlderspensjon())
            spec.heltUttakDato ?: spec.foersteUttakDato // bruker foersteUttakDato som 'backup'-dato
        else
            spec.foersteUttakDato

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