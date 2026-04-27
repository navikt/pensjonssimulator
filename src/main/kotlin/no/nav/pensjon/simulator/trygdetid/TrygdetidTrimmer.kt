package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleLandEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import java.time.LocalDate

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
            .filter { isBeforeNedreDatogrense(it.tomLd, foedselsdato).not() }
            .map {
                if (isBeforeNedreDatogrense(it.fomLd, foedselsdato))
                    adjustFom(it, foedselsdato)
                else
                    it
            }

    private fun removeHoeyAlder(
        trygdetidsperiodeListe: List<TTPeriode>,
        foersteUttakDato: LocalDate,
        foedselsdato: LocalDate
    ): List<TTPeriode> {
        val maxDato: LocalDate = sisteMuligeTrygdetidDato(foedselsdato, foersteUttakDato)

        return trygdetidsperiodeListe
            .filter { it.fomLd!!.isAfter(maxDato).not() }
            .map { adjustTom(periode = it, maxTom = maxDato) }
    }

    private fun isOppholdINorge(grunnlag: TrygdetidOpphold) =
        LandkodeEnum.NOR == grunnlag.periode.landEnum

    private fun isOpptjeningIUtland(grunnlag: TrygdetidOpphold) =
        AvtaleLandEnum.rettTilOpptjeningAvTrygdetid(
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
        trygdetidPeriode.fomLd = nedreDatogrense(foedselsdato)
        return trygdetidPeriode
    }

    private fun adjustTom(periode: TTPeriode, maxTom: LocalDate?): TTPeriode {
        if (periode.tomLd == null || periode.tomLd!!.isAfter(maxTom)) {
            periode.tomLd = maxTom
        }

        return periode
    }

    private fun nedreDatogrense(foedselsdato: LocalDate): LocalDate =
        foedselsdato.plusYears(NEDRE_ALDERSGRENSE.toLong())

    private fun oevreDatogrense(foedselsdato: LocalDate): LocalDate =
        foedselsdato.plusYears(OEVRE_ALDERSGRENSE.toLong())
}