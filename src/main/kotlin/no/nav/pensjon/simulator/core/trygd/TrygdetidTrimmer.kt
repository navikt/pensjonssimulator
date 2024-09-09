package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.util.toLocalDate
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.AdjustPeriodsAtTheEndOfTrygdetidsgrunnlagList
// + AdjustPeriodsAtTheStartOfTrygdetidsgrunnlagList + RemoveTrygdetidsgrunnlagFromIkkeAvtaleland
object TrygdetidTrimmer {

    private const val NEDRE_ALDERSGRENSE = 16
    private const val OEVRE_ALDERSGRENSE = 66

    fun removePeriodBeforeAdulthood(trygdetidPeriodeListe: List<TTPeriode>, foedselDato: LocalDate): List<TTPeriode> =
        trygdetidPeriodeListe
            .filter { !isBeforeNedreDatogrense(it.tom.toLocalDate(), foedselDato) }
            .map {
                if (isBeforeNedreDatogrense(it.fom.toLocalDate(), foedselDato)) adjustFom(it, foedselDato) else it
            }

    fun removePeriodAfterPensionAge(
        trygdetidPeriodeListe: MutableList<TTPeriode>,
        foersteUttakDato: LocalDate,
        foedselDato: LocalDate
    ): List<TTPeriode> {
        val localMaxDato: LocalDate = sisteMuligeTrygdetidDato(foedselDato, foersteUttakDato)
        val maxDato: Date = fromLocalDate(localMaxDato)!!

        return trygdetidPeriodeListe
            .filter { !it.fom!!.after(maxDato) }
            .map { adjustTom(it, maxDato) }
    }

    fun removeIkkeAvtaleland(trygdetidGrunnlagListe: List<TrygdetidOpphold>): List<TTPeriode> =
        trygdetidGrunnlagListe
            .filter { isOppholdINorge(it) || isOpptjeningIUtland(it) }
            .map { it.periode }

    private fun isOppholdINorge(grunnlag: TrygdetidOpphold) =
        Land.NOR.name == grunnlag.periode.land?.kode

    private fun isOpptjeningIUtland(grunnlag: TrygdetidOpphold) =
        TrygdetidOpptjeningRettLand.rettTilOpptjeningAvTrygdetid(
            land = grunnlag.periode.land?.kode?.let(Land::valueOf),
            harArbeidet = grunnlag.arbeidet
        )

    private fun isBeforeNedreDatogrense(dato: LocalDate?, foedselDato: LocalDate): Boolean =
        dato?.isBefore(nedreDatogrense(foedselDato)) == true

    private fun sisteMuligeTrygdetidDato(foedselDato: LocalDate, foersteUttakDato: LocalDate): LocalDate {
        val sisteDatoIAretForOvreDatogrense: LocalDate = getLastDateInYear(oevreDatogrense(foedselDato))

        return if (sisteDatoIAretForOvreDatogrense.isBefore(foersteUttakDato))
            sisteDatoIAretForOvreDatogrense
        else
            getRelativeDateByDays(foersteUttakDato, -1)
    }

    private fun adjustFom(trygdetidPeriode: TTPeriode, foedselDato: LocalDate): TTPeriode {
        trygdetidPeriode.fom = fromLocalDate(nedreDatogrense(foedselDato))
        return trygdetidPeriode
    }

    private fun adjustTom(periode: TTPeriode, maxTom: Date?): TTPeriode {
        if (periode.tom == null || periode.tom!!.after(maxTom)) {
            periode.tom = maxTom
        }

        return periode
    }

    private fun nedreDatogrense(foedselDato: LocalDate): LocalDate =
        getRelativeDateByYear(foedselDato, NEDRE_ALDERSGRENSE)

    private fun oevreDatogrense(foedselDato: LocalDate): LocalDate =
        getRelativeDateByYear(foedselDato, OEVRE_ALDERSGRENSE)
}
