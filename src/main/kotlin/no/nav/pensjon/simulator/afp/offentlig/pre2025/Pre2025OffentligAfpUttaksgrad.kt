package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.validity.BadSpecException
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Uttaksgrader relatert til 'pre-2025' avtalefestet pensjon (AFP) i offentlig sektor
 * (dvs. 'gammel' offentlig AFP), som var enerådende AFP-ordning i offentlig sektor fram til 2025.
 */
@Component
class Pre2025OffentligAfpUttaksgrad(
    private val kravService: KravService,
    private val normalderService: NormertPensjonsalderService
) {
    // SimulerAFPogAPCommand.finnUttaksgradListe
    fun uttaksgradListe(
        spec: SimuleringSpec,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        foedselsdato: LocalDate
    ): MutableList<Uttaksgrad> {
        val ubetingetUttakFom: LocalDate = ubetingetUttakDato(foedselsdato)

        if (forrigeAlderspensjonBeregningResultat == null) {
            return mutableListOf(ubetingetHeltUttak(fom = ubetingetUttakFom))
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        val afpFom: LocalDate = spec.foersteUttakDato!!

        val uttaksgradListe: MutableList<Uttaksgrad> = behandleVedtatteUttaksgrader(eksisterendeKravhode, afpFom)
        // I uttaksgradListe har alle elementer nå en definert t.o.m.-dato

        terminerEksisterendeAlderspensjon(uttaksgradListe, afpFom, ubetingetUttakFom)

        // Ta ut hel alderspensjon ved normert pensjonsalder (dagen etter AFP-periodens slutt):
        uttaksgradListe.add(ubetingetHeltUttak(fom = ubetingetUttakFom))

        return uttaksgradListe
    }

    private fun ubetingetUttakDato(foedselsdato: LocalDate): LocalDate =
        with(normalderService.normalder(foedselsdato)) {
            foedselsdato
                .plusYears(this.aar.toLong())
                .plusMonths(this.maaneder.toLong() + 1)
                .withDayOfMonth(1)
        }

    private companion object {

        /**
         * Sikrer at det ikke tas ut alderspensjon før eller i AFP-perioden,
         * siden tidsbegrenset AFP må være avsluttet før alderspensjonen kan starte.
         */
        private fun terminerEksisterendeAlderspensjon(
            uttaksgradListe: MutableList<Uttaksgrad>,
            afpFom: LocalDate,
            ubetingetUttakFom: LocalDate
        ) {
            if (uttaksgradListe.isEmpty()) return

            val minimumAfpFom: LocalDate = alderspensjonTom(uttaksgradListe).plusDays(1)
            validateAfpStart(afpFom, minimumFom = minimumAfpFom)
            val alderspensjonTerminertFom = minimumAfpFom.coerceAtMost(afpFom)
            // Fra alderspensjonens slutt t.o.m. AFP-periodens slutt må uttaksgraden være 0:
            val afpTom: LocalDate = ubetingetUttakFom.minusDays(1)
            uttaksgradListe.add(uttaksgrad(fom = alderspensjonTerminertFom, grad = 0, tom = afpTom))
        }

        private fun alderspensjonTom(uttaksgradListe: List<Uttaksgrad>): LocalDate =
            uttaksgradListe.maxByOrNull { it.tomDatoLd!! }?.tomDatoLd
                ?: throw IllegalStateException(
                    "uttaksgradListe må inneholde minst ett element og alle elementer må ha definert tomDato"
                )

        private fun ubetingetHeltUttak(fom: LocalDate): Uttaksgrad =
            uttaksgrad(fom, grad = 100, tom = null)

        /**
         * Fjerner siste eksisterende uttaksgradperiode med 0 %,
         * da det skal legges til en 0-periode som dekker AFP-perioden.
         * Terminerer også alderspensjon (setter t.o.m.-dato), da den ikke kan kombineres med tidsbegrenset AFP.
         * NB: Dette gjøres på en kopi av uttaksgradene, slik at de opprinnelige uttaksgradene ikke blir endret.
         */
        private fun behandleVedtatteUttaksgrader(
            kravhode: Kravhode?,
            afpFom: LocalDate
        ): MutableList<Uttaksgrad> {
            val uttaksgradListe = kravhode?.uttaksgradListe.orEmpty().map { it.copy() }.toMutableList()

            val listeUtenSiste0Uttak =
                siste0UttakFom(uttaksgradListe)?.let { listeUten0Uttak(uttaksgradListe, fom = it) }
                    ?: uttaksgradListe

            replaceNullTom(uttaksgradListe = listeUtenSiste0Uttak, tom = afpFom.minusDays(1))
            return listeUtenSiste0Uttak
        }

        private fun listeUten0Uttak(uttaksgradListe: List<Uttaksgrad>, fom: LocalDate): MutableList<Uttaksgrad> =
            uttaksgradListe
                .filterNot { it.uttaksgrad == 0 && it.fomDatoLd == fom }
                .toMutableList()

        private fun siste0UttakFom(uttaksgradListe: List<Uttaksgrad>): LocalDate? =
            uttaksgradListe
                .filter { it.uttaksgrad == 0 }
                .mapNotNull { it.fomDatoLd }
                .maxOrNull()

        // PEN: SimulerAFPogAPCommandHelper.updateUttaksgradWithTomDateNull
        private fun replaceNullTom(uttaksgradListe: List<Uttaksgrad>, tom: LocalDate) {
            uttaksgradListe.forEach {
                if (it.tomDatoLd == null) {
                    it.tomDatoLd = tom
                }
            }
        }

        private fun uttaksgrad(fom: LocalDate?, grad: Int, tom: LocalDate?) =
            Uttaksgrad().apply {
                fomDatoLd = fom
                tomDatoLd = tom
                uttaksgrad = grad
            }

        private fun validateAfpStart(afpFom: LocalDate, minimumFom: LocalDate) {
            if (afpFom.isBefore(minimumFom)) {
                throw BadSpecException(
                    "For tidlig uttak av AFP (må starte etter alderspensjonsperiodens slutt, dvs. tidligst $minimumFom)"
                )
            }
        }
    }
}
