package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.LOCAL_ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.calculateAgeInYears
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYesterday
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterToday
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeToday
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.setTimeToZero
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.toLocalDate
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SettTrygdetidHelper
object TrygdetidSetter {

    private const val NEDRE_ALDERSGRENSE = 16

    fun settTrygdetid(spec: TrygdetidGrunnlagSpec): Persongrunnlag {
        val persongrunnlag = spec.persongrunnlag
        val utlandAntallAar = spec.utlandAntallAar ?: 0
        val tom = spec.tom
        val forrigeAlderspensjonBeregningResult = spec.forrigeAlderspensjonBeregningResultat
        val foersteUttakDato = spec.simuleringSpec.foersteUttakDato

        if (forrigeAlderspensjonBeregningResult == null) {
            settTrygdetidUtenTidligereBeregningsresultat(persongrunnlag, utlandAntallAar, tom, foersteUttakDato)
        } else {
            settTrygdetidMedTidligereBeregningsresultat(persongrunnlag, tom)
        }

        return persongrunnlag
    }

    // SettTrygdetidHelper.findAntallArMedOpptjening
    private fun antallAarMedOpptjening(
        opptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag>,
        foedselDato: Date?
    ): Int {
        val aarMedOpptjeningSet: SortedSet<Int> = TreeSet()

        opptjeningGrunnlagListe.forEach {
            if (it.pp > 0.0) {
                aarMedOpptjeningSet.add(it.ar)
            }
        }

        return TrygdetidUtil.antallAarMedOpptjening(
            opptjeningAarSet = aarMedOpptjeningSet,
            aarSoekerFikkMinstealderForTrygdetid = yearUserTurnsGivenAge(foedselDato!!, NEDRE_ALDERSGRENSE),
            dagensDato = fromLocalDate(LocalDate.now())!!
        )
    }

    // SettTrygdetidHelper.settTrygdetidWithNoPreviousBerRes
    private fun settTrygdetidUtenTidligereBeregningsresultat(
        persongrunnlag: Persongrunnlag,
        utlandAntallAar: Int,
        tom: LocalDate?,
        foersteUttakDato: LocalDate?
    ) {
        val datoSokerFikkMinstealderForTrygdetid: LocalDate =
            getRelativeDateByYear(persongrunnlag.fodselsdato!!, NEDRE_ALDERSGRENSE).toLocalDate()!!

        addKapittel19TrygdetidPerioder(
            persongrunnlag,
            utlandAntallAar,
            tom,
            datoSokerFikkMinstealderForTrygdetid,
            foersteUttakDato
        )

        addKapittel20TrygdetidPerioder(persongrunnlag, utlandAntallAar, tom, datoSokerFikkMinstealderForTrygdetid)
    }

    // SettTrygdetidHelper.settTrygdetidGivenPreviousBerRes
    private fun settTrygdetidMedTidligereBeregningsresultat(persongrunnlag: Persongrunnlag, tom: LocalDate?) {
        conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioder, tom)
        conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioderKapittel20, tom)

        if (tom == null || isAfterToday(fromLocalDate(tom))) {
            val trygdetidPeriode = newNorskTrygdetidPeriode(LocalDate.now(), tom, true)
            persongrunnlag.trygdetidPerioder.add(trygdetidPeriode)
            persongrunnlag.trygdetidPerioderKapittel20.add(TTPeriode(trygdetidPeriode).also { it.finishInit() })
        }
    }

    // SettTrygdetidHelper.addTrygdetidsgrunnlagKap19
    private fun addKapittel19TrygdetidPerioder(
        persongrunnlag: Persongrunnlag,
        utlandAntallAar: Int,
        tom: LocalDate?,
        datoSoekerFikkMinstealderForTrygdetid: LocalDate,
        foersteUttakDato: LocalDate?
    ) {
        val aarMedOpptjening = antallAarMedOpptjening(persongrunnlag.opptjeningsgrunnlagListe, persongrunnlag.fodselsdato)
        val alderVedUttak = calculateAgeInYears(persongrunnlag.fodselsdato!!, foersteUttakDato!!)
        val maxAntallAarUtland = alderVedUttak - NEDRE_ALDERSGRENSE - aarMedOpptjening
        val kapittel19UtlandAntallAar = if (utlandAntallAar > maxAntallAarUtland) maxAntallAarUtland else utlandAntallAar
        val fom = getRelativeDateByYear(datoSoekerFikkMinstealderForTrygdetid, kapittel19UtlandAntallAar)

        if (isBeforeByDay(fom, tom ?: LOCAL_ETERNITY, false)) {
            persongrunnlag.trygdetidPerioder.add(newNorskTrygdetidPeriode(fom, tom, false))
        }
    }

    // SettTrygdetidHelper.addTrygdetidsgrunnlagKap20
    private fun addKapittel20TrygdetidPerioder(
        persongrunnlag: Persongrunnlag,
        utlandAntallAar: Int,
        tom: LocalDate?,
        dateUserTurned16Years: LocalDate
    ) {
        val fom: LocalDate = getRelativeDateByYear(dateUserTurned16Years, utlandAntallAar)

        if (isBeforeByDay(fom, tom ?: LOCAL_ETERNITY, false)) {
            persongrunnlag.trygdetidPerioderKapittel20.add(newNorskTrygdetidPeriode(fom, tom, false))
        }
    }

    // SettTrygdetidHelper.conditionallyAdjustLastTrygdetidsgrunnlag
    private fun conditionallyAdjustLastTrygdetidPeriode(periodeListe: List<TTPeriode>, tom: LocalDate?) {
        val twoYearsBeforeToday = getRelativeDateByYear(fromLocalDate(LocalDate.now())!!, -2)
        val relevantePerioder = periodeListe.filter {
            isDateInPeriod(twoYearsBeforeToday, it.fom, it.tom) || isAfterByDay(
                it.fom,
                twoYearsBeforeToday,
                false
            )
        }
        if (relevantePerioder.isEmpty()) return

        findLatest(periodeListe)?.apply {
            this.tom = finalTom(fromLocalDate(tom))
            this.poengIUtAr = false
        }
    }

    // Extracted from SettTrygdetidHelper.conditionallyAdjustLastTrygdetidsgrunnlag
    private fun finalTom(tom: Date?): Date {
        val validTom = if (tom == null || !isBeforeToday(tom)) getYesterday() else tom
        return setTimeToZero(validTom)
    }

    // PeriodisertInformasjonListeUtils.findLatest
    private fun findLatest(list: List<TTPeriode>): TTPeriode? {
        if (list.isEmpty()) return null

        if (list.size == 1) {
            return list[0]
        }

        var result: TTPeriode? = null
        var latestDate: Date? = null

        for (element in list) {
            if (isAfterByDay(element.fom, latestDate, false)) {
                result = element
                latestDate = element.fom
            }
        }

        return result
    }

    // SettTrygdetidHelper.createTrygdetidsgrunnlagNorge
    private fun newNorskTrygdetidPeriode(fom: LocalDate, tom: LocalDate?, ikkeProRata: Boolean) =
        TrygdetidGrunnlagFactory.trygdetidPeriode(
            fom = fromLocalDate(fom)!!, //TODO noon?
            tom = fromLocalDate(tom),
            land = LandkodeEnum.NOR,
            ikkeProRata = ikkeProRata,
            bruk = true
        )
}
