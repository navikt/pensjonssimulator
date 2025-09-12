package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.LOCAL_ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.calculateAgeInYears
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.trygdetidPeriode
import no.nav.pensjon.simulator.trygdetid.TrygdetidUtil.antallAarMedOpptjening
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SettTrygdetidHelper
@Component
class TrygdetidSetter(private val time: Time) {

    fun settTrygdetid(spec: TrygdetidGrunnlagSpec): Persongrunnlag {
        val persongrunnlag = spec.persongrunnlag
        val utlandAntallAar = spec.utlandAntallAar ?: 0
        val tom = spec.tom
        val forrigeAlderspensjonBeregningResult = spec.forrigeAlderspensjonBeregningResultat
        val foersteUttakDato = spec.simuleringSpec.foersteUttakDato

        if (forrigeAlderspensjonBeregningResult == null)
            settTrygdetidUtenTidligereBeregningsresultat(persongrunnlag, utlandAntallAar, tom, foersteUttakDato)
        else
            settTrygdetidMedTidligereBeregningsresultat(persongrunnlag, tom)

        return persongrunnlag
    }

    // PEN: SettTrygdetidHelper.settTrygdetidWithNoPreviousBerRes
    private fun settTrygdetidUtenTidligereBeregningsresultat(
        persongrunnlag: Persongrunnlag,
        utlandAntallAar: Int,
        tom: LocalDate?,
        foersteUttakDato: LocalDate?
    ) {
        val datoSoekerFikkMinstealderForTrygdetid: LocalDate =
            getRelativeDateByYear(persongrunnlag.fodselsdato!!, NEDRE_ALDERSGRENSE).toNorwegianLocalDate()

        addKapittel19TrygdetidPerioder(
            persongrunnlag,
            utlandAntallAar,
            tom,
            datoSoekerFikkMinstealderForTrygdetid,
            foersteUttakDato
        )

        addKapittel20TrygdetidPerioder(persongrunnlag, utlandAntallAar, tom, datoSoekerFikkMinstealderForTrygdetid)
    }

    // PEN: SettTrygdetidHelper.settTrygdetidGivenPreviousBerRes
    private fun settTrygdetidMedTidligereBeregningsresultat(persongrunnlag: Persongrunnlag, tom: LocalDate?) {
        conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioder, tom)
        conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioderKapittel20, tom)

        if (tom == null || tom.isAfter(time.today())) {
            val trygdetidPeriode = newNorskTrygdetidPeriode(fom = time.today(), tom, ikkeProRata = true)
            persongrunnlag.trygdetidPerioder.add(trygdetidPeriode)
            persongrunnlag.trygdetidPerioderKapittel20.add(TTPeriode(trygdetidPeriode).also { it.finishInit() })
        }
    }

    // PEN: SettTrygdetidHelper.conditionallyAdjustLastTrygdetidsgrunnlag
    private fun conditionallyAdjustLastTrygdetidPeriode(periodeListe: List<TTPeriode>, tom: LocalDate?) {
        val twoYearsBeforeToday =
            getRelativeDateByYear(time.today().toNorwegianDateAtNoon(), -2) //TODO magic value

        val relevantePerioder = periodeListe.filter {
            isDateInPeriod(twoYearsBeforeToday, it.fom, it.tom) ||
                    isAfterByDay(thisDate = it.fom, thatDate = twoYearsBeforeToday, allowSameDay = false)
        }

        if (relevantePerioder.isEmpty()) return

        findLatest(periodeListe)?.apply { // NB: Using periodeListe, not relevantePerioder (same as in PEN)
            this.tom = limitToYesterday(tom).toNorwegianDateAtNoon()
            this.poengIUtAr = false
        }
    }

    // PEN: SettTrygdetidHelper.addTrygdetidsgrunnlagKap19
    private fun addKapittel19TrygdetidPerioder(
        persongrunnlag: Persongrunnlag,
        utlandAntallAar: Int,
        tom: LocalDate?,
        datoSoekerOppnaaddeMinstealderForTrygdetid: LocalDate,
        foersteUttakDato: LocalDate?
    ) {
        val aarMedOpptjening =
            antallAarMedOpptjening(persongrunnlag.opptjeningsgrunnlagListe, persongrunnlag.fodselsdato)
        val alderVedUttak = calculateAgeInYears(persongrunnlag.fodselsdato!!, foersteUttakDato!!)
        val maxAntallAarUtland = alderVedUttak - NEDRE_ALDERSGRENSE - aarMedOpptjening
        val kapittel19UtlandAntallAar =
            if (utlandAntallAar > maxAntallAarUtland) maxAntallAarUtland else utlandAntallAar
        val fom = getRelativeDateByYear(datoSoekerOppnaaddeMinstealderForTrygdetid, kapittel19UtlandAntallAar)

        if (isBeforeByDay(fom, tom ?: LOCAL_ETERNITY, false)) {
            persongrunnlag.trygdetidPerioder.add(newNorskTrygdetidPeriode(fom, tom, false))
        }
    }

    // PEN: SettTrygdetidHelper.findAntallArMedOpptjening
    private fun antallAarMedOpptjening(
        opptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag>,
        foedselsdato: Date?
    ): Int {
        val aarMedOpptjeningSet: SortedSet<Int> = TreeSet()

        opptjeningGrunnlagListe.forEach {
            if (it.pp > 0.0) {
                aarMedOpptjeningSet.add(it.ar)
            }
        }

        return antallAarMedOpptjening(
            opptjeningAarSet = aarMedOpptjeningSet,
            aarSoekerFikkMinstealderForTrygdetid = yearUserTurnsGivenAge(foedselsdato!!, NEDRE_ALDERSGRENSE),
            dagensDato = time.today()
        )
    }

    // Extracted from SettTrygdetidHelper.conditionallyAdjustLastTrygdetidsgrunnlag in PEN
    private fun limitToYesterday(tom: LocalDate?): LocalDate =
        if (tom?.isBefore(time.today()) == true) tom
        else yesterday()

    // PEN: no.stelvio.common.util.DateUtil.getYesterday + getRelativeDateFromNow
    private fun yesterday(): LocalDate =
        time.today().minusDays(1)

    private companion object {
        private const val NEDRE_ALDERSGRENSE = 16

        // PEN: SettTrygdetidHelper.addTrygdetidsgrunnlagKap20
        private fun addKapittel20TrygdetidPerioder(
            persongrunnlag: Persongrunnlag,
            utlandAntallAar: Int,
            tom: LocalDate?,
            datoSoekerOppnaaddeMinstealderForTrygdetid: LocalDate
        ) {
            val fom: LocalDate = getRelativeDateByYear(datoSoekerOppnaaddeMinstealderForTrygdetid, utlandAntallAar)

            if (isBeforeByDay(fom, tom ?: LOCAL_ETERNITY, allowSameDay = false)) {
                persongrunnlag.trygdetidPerioderKapittel20.add(newNorskTrygdetidPeriode(fom, tom, ikkeProRata = false))
            }
        }

        // PEN: PeriodisertInformasjonListeUtils.findLatest
        private fun findLatest(list: List<TTPeriode>): TTPeriode? {
            if (list.isEmpty()) return null
            if (list.size == 1) return list[0]

            var result: TTPeriode? = null
            var latestDate: Date? = null

            for (element in list) {
                if (isAfterByDay(element.fom, latestDate, allowSameDay = false)) {
                    result = element
                    latestDate = element.fom
                }
            }

            return result
        }

        // PEN: SettTrygdetidHelper.createTrygdetidsgrunnlagNorge
        private fun newNorskTrygdetidPeriode(fom: LocalDate, tom: LocalDate?, ikkeProRata: Boolean) =
            trygdetidPeriode(
                fom = fom.toNorwegianDateAtNoon(),
                tom = tom?.toNorwegianDateAtNoon(),
                land = LandkodeEnum.NOR,
                ikkeProRata,
                bruk = null // bruk is not set in SettTrygdetidHelper.createTrygdetidsgrunnlagNorge in PEN
            )
    }
}
