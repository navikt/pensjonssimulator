package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
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
        val ubetingetUttakDato: LocalDate = ubetingetUttakDato(foedselsdato)

        if (forrigeAlderspensjonBeregningResultat == null) {
            return mutableListOf(ubetingetHeltUttak(fom = ubetingetUttakDato))
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        val afpFom: LocalDate = spec.foersteUttakDato!!
        val uttaksgradListe: MutableList<Uttaksgrad> = behandleVedtatteUttaksgrader(eksisterendeKravhode, afpFom)
        val minimumAfpFom: LocalDate? = alderspensjonTom(uttaksgradListe)?.plusDays(1)
        minimumAfpFom?.let { validateAfpStart(afpFom, minimumFom = it) }
        val alderspensjonTerminertFom = minimumAfpFom?.coerceAtMost(afpFom)

        // Fra alderspensjonens slutt t.o.m. AFP-periodens slutt må uttaksgraden være 0:
        val afpTom: LocalDate = ubetingetUttakDato.minusDays(1)
        uttaksgradListe.add(uttaksgrad(fom = alderspensjonTerminertFom, grad = 0, tom = afpTom))

        // Ta ut hel alderspensjon ved normert pensjonsalder (dagen etter AFP-periodens slutt):
        uttaksgradListe.add(ubetingetHeltUttak(fom = ubetingetUttakDato))

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

        private fun alderspensjonTom(uttaksgradListe: List<Uttaksgrad>): LocalDate? =
            uttaksgradListe.maxByOrNull { it.tomDato!! }?.tomDato?.toNorwegianLocalDate()

        private fun ubetingetHeltUttak(fom: LocalDate): Uttaksgrad =
            uttaksgrad(fom, grad = 100, tom = null)

        private fun behandleVedtatteUttaksgrader(
            kravhode: Kravhode?,
            afpFom: LocalDate
        ): MutableList<Uttaksgrad> {
            // Fjern eksisterende uttaksgradperioder med 0 %, da det skal legges til en 0-periode som dekker AFP-perioden:
            val uttaksgradListe = uttaksgraderUten0(kravhode).toMutableList()

            // Terminer alderspensjon (kan ikke kombineres med tidsbegrenset AFP):
            replaceNullTom(uttaksgradListe, tom = afpFom.minusDays(1))

            return uttaksgradListe
        }

        private fun uttaksgraderUten0(kravhode: Kravhode?): List<Uttaksgrad> =
            kravhode?.uttaksgradListe.orEmpty().filter { it.uttaksgrad != 0 }

        // PEN: SimulerAFPogAPCommandHelper.updateUttaksgradWithTomDateNull
        private fun replaceNullTom(uttaksgradListe: List<Uttaksgrad>, tom: LocalDate) {
            uttaksgradListe.forEach {
                if (it.tomDato == null) {
                    it.tomDato = tom.toNorwegianDateAtNoon()
                    it.setDatesToNoon()
                }
            }
        }

        private fun uttaksgrad(fom: LocalDate?, grad: Int, tom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fom?.toNorwegianDateAtNoon()
                tomDato = tom?.toNorwegianDateAtNoon()
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
