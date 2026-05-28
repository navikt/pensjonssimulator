package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.LOCAL_ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.calculateAgeInYears
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.InnlandTrygdetidUtil.norskTrygdetidPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidUtil.antallAarMedOpptjening
import org.springframework.stereotype.Component
import java.time.LocalDate

// PEN:
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SettTrygdetidHelper
@Component
class TrygdetidSetter(
    private val adjuster: TrygdetidAdjuster,
    private val time: Time
) {
    fun settTrygdetid(spec: TrygdetidsgrunnlagAarsbasertSpec, persongrunnlag: Persongrunnlag): Persongrunnlag {
        val tom = spec.tom

        if (spec.erFoerstegangsberegning)
            settTrygdetidUtenTidligereBeregningsresultat(
                persongrunnlag,
                angittUtlandAntallAar = spec.antallAarUtenlands,
                tom,
                foersteUttakDato = spec.foersteUttakDato
            )
        else
            settTrygdetidMedTidligereBeregningsresultat(persongrunnlag, fom = time.today(), tom)

        return persongrunnlag
    }

    // PEN: SettTrygdetidHelper.settTrygdetidWithNoPreviousBerRes
    private fun settTrygdetidUtenTidligereBeregningsresultat(
        persongrunnlag: Persongrunnlag,
        angittUtlandAntallAar: Int,
        tom: LocalDate?,
        foersteUttakDato: LocalDate
    ) {
        val foedselsdato: LocalDate = persongrunnlag.fodselsdatoLd!!

        val maxUtlandAntallAar = kapittel19MaxUtlandAntallAar(
            foedselsdato,
            foersteUttakDato,
            opptjeningListe = persongrunnlag.opptjeningsgrunnlagListe
        )

        // NB: Spesiell behandling av trygdetid i.h.t. kapittel 19:
        // Antall år utenlands begrenses slik at trygdetid ikke starter etter uttak.
        norskTrygdetidPeriode(
            foedselsdato,
            utlandAntallAar = angittUtlandAntallAar.coerceAtMost(maxUtlandAntallAar),
            tom
        )?.let { persongrunnlag.trygdetidPerioder.add(it) }

        norskTrygdetidPeriode(
            foedselsdato,
            utlandAntallAar = angittUtlandAntallAar,
            tom
        )?.let { persongrunnlag.trygdetidPerioderKapittel20.add(it) }
    }

    // PEN: SettTrygdetidHelper.addTrygdetidsgrunnlagKap20
    // (also part of SettTrygdetidHelper.addTrygdetidsgrunnlagKap19)
    private fun norskTrygdetidPeriode(
        foedselsdato: LocalDate,
        utlandAntallAar: Int, // antall år etter nedre aldersgrense for trygdetid
        tom: LocalDate?
    ): TTPeriode? {
        val trygdetidStartAlderAar: Int = NEDRE_ALDERSGRENSE + utlandAntallAar
        val fom: LocalDate = foedselsdato.plusYears(trygdetidStartAlderAar.toLong())

        return if (isBeforeByDay(fom, tom ?: LOCAL_ETERNITY, allowSameDay = false))
            norskTrygdetidPeriode(fom, tom, ikkeProRata = false)
        else
            null
    }

    // PEN: SettTrygdetidHelper.settTrygdetidGivenPreviousBerRes
    private fun settTrygdetidMedTidligereBeregningsresultat(
        persongrunnlag: Persongrunnlag,
        fom: LocalDate,
        tom: LocalDate?
    ) {
        adjuster.conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioder, tom)
        adjuster.conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioderKapittel20, tom)

        if (tom == null || tom.isAfter(fom)) {
            with(norskTrygdetidPeriode(fom, tom, ikkeProRata = true)) {
                persongrunnlag.trygdetidPerioder.add(this)
                persongrunnlag.trygdetidPerioderKapittel20.add(this.copy())
            }
        }
    }

    private fun kapittel19MaxUtlandAntallAar(
        foedselsdato: LocalDate,
        foersteUttakDato: LocalDate,
        opptjeningListe: List<Opptjeningsgrunnlag>
    ): Int {
        val opptjeningAntallAar = kapittel19OpptjeningAntallAar(opptjeningListe, foedselsdato)
        val uttakAlderAar = calculateAgeInYears(foedselsdato, foersteUttakDato)
        return uttakAlderAar - NEDRE_ALDERSGRENSE - opptjeningAntallAar
    }

    // PEN: SettTrygdetidHelper.findAntallArMedOpptjening
    private fun kapittel19OpptjeningAntallAar(
        opptjeningListe: List<Opptjeningsgrunnlag>,
        foedselsdato: LocalDate
    ): Int =
        antallAarMedOpptjening(
            registrerteAarMedOpptjening = opptjeningListe.filter { it.pp > 0.0 }.map { it.ar }.toSortedSet(),
            aarSoekerFikkMinstealderForTrygdetid = yearUserTurnsGivenAge(foedselsdato, NEDRE_ALDERSGRENSE),
            dagensDato = time.today()
        )

    private companion object {
        private const val NEDRE_ALDERSGRENSE = 16
    }
}