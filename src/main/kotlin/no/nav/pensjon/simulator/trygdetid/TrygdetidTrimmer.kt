package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpphold
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpptjeningRettLand.Companion.rettTilOpptjeningAvTrygdetid
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.AdjustPeriodsAtTheEndOfTrygdetidsgrunnlagList
// + AdjustPeriodsAtTheStartOfTrygdetidsgrunnlagList + RemoveTrygdetidsgrunnlagFromIkkeAvtaleland
object TrygdetidTrimmer {

    private const val NEDRE_ALDERSGRENSE = 16
    private const val OEVRE_ALDERSGRENSE = 66

    fun aldersbegrens(
        trygdetidsperiodeListe: List<TTPeriode>,
        foersteUttakDato: LocalDate,
        foedselsdato: LocalDate
    ): List<TTPeriode> {
        val trimmedListe = removeLavAlder(trygdetidsperiodeListe, foedselsdato)
        return removeHoeyAlder(trygdetidsperiodeListe = trimmedListe, foersteUttakDato, foedselsdato)
    }

    fun removeIkkeAvtaleland(trygdetidGrunnlagListe: List<TrygdetidOpphold>): List<TTPeriode> =
        trygdetidGrunnlagListe
            .filter { isOppholdINorge(it) || isOpptjeningIUtland(it) }
            .map { it.periode }

    private fun removeLavAlder(trygdetidPeriodeListe: List<TTPeriode>, foedselsdato: LocalDate): List<TTPeriode> =
        trygdetidPeriodeListe
            .filter { isBeforeNedreDatogrense(it.tom?.toNorwegianLocalDate(), foedselsdato).not() }
            .map {
                if (isBeforeNedreDatogrense(it.fom?.toNorwegianLocalDate(), foedselsdato))
                    adjustFom(it, foedselsdato)
                else
                    it
            }

    private fun removeHoeyAlder(
        trygdetidsperiodeListe: List<TTPeriode>,
        foersteUttakDato: LocalDate,
        foedselsdato: LocalDate
    ): List<TTPeriode> {
        val maxDato: Date = sisteMuligeTrygdetidDato(foedselsdato, foersteUttakDato).toNorwegianDateAtNoon()

        return trygdetidsperiodeListe
            .filter { it.fom!!.after(maxDato).not() }
            .map { adjustTom(periode = it, maxTom = maxDato) }
    }

    private fun isOppholdINorge(grunnlag: TrygdetidOpphold) =
        LandkodeEnum.NOR == grunnlag.periode.landEnum

    private fun isOpptjeningIUtland(grunnlag: TrygdetidOpphold) =
        rettTilOpptjeningAvTrygdetid(
            land = grunnlag.periode.landEnum,
            harArbeidet = grunnlag.arbeidet
        )

    private fun isBeforeNedreDatogrense(dato: LocalDate?, foedselsdato: LocalDate): Boolean =
        dato?.isBefore(nedreDatogrense(foedselsdato)) == true

    private fun sisteMuligeTrygdetidDato(foedselsdato: LocalDate, foersteUttakDato: LocalDate): LocalDate {
        val sisteDatoIAaretForOevreDatogrense: LocalDate = sisteDag(aar = oevreDatogrense(foedselsdato).year)

        return if (sisteDatoIAaretForOevreDatogrense.isBefore(foersteUttakDato))
            sisteDatoIAaretForOevreDatogrense
        else
            foersteUttakDato.minusDays(1)
    }

    private fun adjustFom(trygdetidPeriode: TTPeriode, foedselsdato: LocalDate): TTPeriode {
        trygdetidPeriode.fom = nedreDatogrense(foedselsdato).toNorwegianDateAtNoon()
        return trygdetidPeriode
    }

    private fun adjustTom(periode: TTPeriode, maxTom: Date?): TTPeriode {
        if (periode.tom == null || periode.tom!!.after(maxTom)) {
            periode.tom = maxTom
        }

        return periode
    }

    private fun nedreDatogrense(foedselsdato: LocalDate): LocalDate =
        foedselsdato.plusYears(NEDRE_ALDERSGRENSE.toLong())

    private fun oevreDatogrense(foedselsdato: LocalDate): LocalDate =
        foedselsdato.plusYears(OEVRE_ALDERSGRENSE.toLong())
}