package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.LOCAL_ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.calculateAgeInYears
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.trygdetid.InnlandTrygdetidUtil.norskTrygdetidPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidUtil.antallAarMedOpptjening
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SettTrygdetidHelper
@Component
class TrygdetidSetter(
    private val adjuster: TrygdetidAdjuster,
    private val time: Time
) {
    fun settTrygdetid(spec: TrygdetidGrunnlagSpec): Persongrunnlag {
        val persongrunnlag = spec.persongrunnlag
        val tom = spec.tom

        if (spec.forrigeAlderspensjonBeregningResultat == null)
            settTrygdetidUtenTidligereBeregningsresultat(
                persongrunnlag,
                angittUtlandAntallAar = spec.utlandAntallAar ?: 0,
                tom,
                foersteUttakDato = spec.simuleringSpec.foersteUttakDato!!
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
        val foedselsdato: Date = persongrunnlag.fodselsdato!!
        val localFoedselsdato: LocalDate = foedselsdato.toNorwegianLocalDate()

        norskTrygdetidPeriode(
            localFoedselsdato,
            utlandAntallAar = kapittel19UtlandAntallAar(
                angittUtlandAntallAar,
                foedselsdato,
                foersteUttakDato,
                opptjeningsgrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe
            ),
            tom
        )?.let { persongrunnlag.trygdetidPerioder.add(it) }

        norskTrygdetidPeriode(
            localFoedselsdato,
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
                persongrunnlag.trygdetidPerioderKapittel20.add(TTPeriode(this))
            }
        }
    }

    /**
     * NB: Spesiell behandling av trygdetid i.h.t. kapittel 19:
     * Antall år utenlands begrenses slik at trygdetid ikke starter etter uttak.
     */
    private fun kapittel19UtlandAntallAar(
        utlandAntallAar: Int,
        foedselsdato: Date,
        foersteUttakDato: LocalDate,
        opptjeningsgrunnlagListe: List<Opptjeningsgrunnlag>
    ): Int {
        val opptjeningAntallAar = kapittel19OpptjeningAntallAar(opptjeningsgrunnlagListe, foedselsdato)
        val uttakAlderAar = calculateAgeInYears(foedselsdato.toNorwegianLocalDate(), foersteUttakDato)
        val maxAntallAarUtland = uttakAlderAar - NEDRE_ALDERSGRENSE - opptjeningAntallAar
        return utlandAntallAar.coerceAtMost(maxAntallAarUtland)
    }

    // PEN: SettTrygdetidHelper.findAntallArMedOpptjening
    private fun kapittel19OpptjeningAntallAar(opptjeningListe: List<Opptjeningsgrunnlag>, foedselsdato: Date): Int {
        val registrerteAarMedOpptjening: SortedSet<Int> =
            opptjeningListe.filter { it.pp > 0.0 }.map { it.ar }.toSortedSet()

        return antallAarMedOpptjening(
            registrerteAarMedOpptjening,
            aarSoekerFikkMinstealderForTrygdetid = yearUserTurnsGivenAge(foedselsdato, NEDRE_ALDERSGRENSE),
            dagensDato = time.today()
        )
    }

    private companion object {
        private const val NEDRE_ALDERSGRENSE = 16
    }
}