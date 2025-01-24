package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.AdjustPeriodsAtTheEndOfTrygdetidsgrunnlagList
// + AdjustPeriodsAtTheStartOfTrygdetidsgrunnlagList + RemoveTrygdetidsgrunnlagFromIkkeAvtaleland
object TrygdetidTrimmer {

    private const val NEDRE_ALDERSGRENSE = 16
    private const val OEVRE_ALDERSGRENSE = 66

    fun removePeriodBeforeAdulthood(trygdetidPeriodeListe: List<TTPeriode>, foedselsdato: LocalDate): List<TTPeriode> =
        trygdetidPeriodeListe
            .filter { !isBeforeNedreDatogrense(it.tom?.toNorwegianLocalDate(), foedselsdato) }
            .map {
                if (isBeforeNedreDatogrense(it.fom?.toNorwegianLocalDate(), foedselsdato))
                    adjustFom(it, foedselsdato)
                else
                    it
            }

    fun removePeriodAfterPensionAge(
        trygdetidPeriodeListe: MutableList<TTPeriode>,
        foersteUttakDato: LocalDate,
        foedselsdato: LocalDate
    ): List<TTPeriode> {
        val localMaxDato: LocalDate = sisteMuligeTrygdetidDato(foedselsdato, foersteUttakDato)
        val maxDato: Date = localMaxDato.toNorwegianDateAtNoon()

        return trygdetidPeriodeListe
            .filter { !it.fom!!.after(maxDato) }
            .map { adjustTom(it, maxDato) }
    }

    fun removeIkkeAvtaleland(trygdetidGrunnlagListe: List<TrygdetidOpphold>): List<TTPeriode> =
        trygdetidGrunnlagListe
            .filter { isOppholdINorge(it) || isOpptjeningIUtland(it) }
            .map { it.periode }

    private fun isOppholdINorge(grunnlag: TrygdetidOpphold) =
        LandkodeEnum.NOR.name == grunnlag.periode.land?.kode

    private fun isOpptjeningIUtland(grunnlag: TrygdetidOpphold) =
        TrygdetidOpptjeningRettLand.rettTilOpptjeningAvTrygdetid(
            land = grunnlag.periode.land?.kode?.let(LandkodeEnum::valueOf),
            harArbeidet = grunnlag.arbeidet
        )

    private fun isBeforeNedreDatogrense(dato: LocalDate?, foedselDato: LocalDate): Boolean =
        dato?.isBefore(nedreDatogrense(foedselDato)) == true

    private fun sisteMuligeTrygdetidDato(foedselDato: LocalDate, foersteUttakDato: LocalDate): LocalDate {
        val sisteDatoIAaretForOevreDatogrense: LocalDate = getLastDateInYear(oevreDatogrense(foedselDato))

        return if (sisteDatoIAaretForOevreDatogrense.isBefore(foersteUttakDato))
            sisteDatoIAaretForOevreDatogrense
        else
            getRelativeDateByDays(foersteUttakDato, -1)
    }

    private fun adjustFom(trygdetidPeriode: TTPeriode, foedselDato: LocalDate): TTPeriode {
        trygdetidPeriode.fom = nedreDatogrense(foedselDato).toNorwegianDateAtNoon()
        return trygdetidPeriode
    }

    private fun adjustTom(periode: TTPeriode, maxTom: Date?): TTPeriode {
        if (periode.tom == null || periode.tom!!.after(maxTom)) {
            periode.tom = maxTom
        }

        return periode
    }

    private fun nedreDatogrense(foedselsdato: LocalDate): LocalDate =
        getRelativeDateByYear(foedselsdato, NEDRE_ALDERSGRENSE)

    private fun oevreDatogrense(foedselsdato: LocalDate): LocalDate =
        getRelativeDateByYear(foedselsdato, OEVRE_ALDERSGRENSE)
}
