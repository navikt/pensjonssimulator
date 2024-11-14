package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.normalder.NormAlderService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Uttaksgrader relatert til 'pre-2025' avtalefestet pensjon (AFP) i offentlig sektor
 * (dvs. 'gammel' offentlig AFP, som erstattes av ny ordning fra 2025).
 */
@Component
class Pre2025OffentligAfpUttakGrad(
    private val kravService: KravService,
    private val normAlderService: NormAlderService
) {
    // SimulerAFPogAPCommand.finnUttaksgradListe
    fun uttakGradListe(
        spec: SimuleringSpec,
        forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
        foedselDato: LocalDate
    ): MutableList<Uttaksgrad> {
        val ubetingetUttakDato: LocalDate = ubetingetUttakDato(foedselDato)

        if (forrigeAlderspensjonBeregningResultat == null) {
            return mutableListOf(uttakGrad(fom = ubetingetUttakDato, grad = 100, tom = null))
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        val uttakGradListe: MutableList<Uttaksgrad> = mutableListOf()
        uttakGradListe.addAll(copy(eksisterendeKravhode?.uttaksgradListe.orEmpty()))
        spec.foersteUttakDato?.let { replaceNullTom(uttakGradListe, it.minusDays(1)) }

        val foersteTom: LocalDate = ubetingetUttakDato.minusDays(1)
        uttakGradListe.add(uttakGrad(fom = spec.foersteUttakDato, grad = 0, tom = foersteTom))
        uttakGradListe.add(uttakGrad(fom = ubetingetUttakDato, grad = 100, tom = null))
        return uttakGradListe
    }

    private fun ubetingetUttakDato(foedselDato: LocalDate): LocalDate =
        with(normAlderService.normAlder(foedselDato)) {
            foedselDato
                .plusYears(this.aar.toLong())
                .plusMonths(this.maaneder.toLong() + 1)
                .withDayOfMonth(1)
        }

    private companion object {
        // SimulerAFPogAPCommandHelper.makeCopyOfUttaksgradList
        private fun copy(liste: List<Uttaksgrad>): List<Uttaksgrad> {
            val copy: MutableList<Uttaksgrad> = mutableListOf()
            liste.forEach { copy.add(Uttaksgrad(it)) }
            return copy
        }

        // SimulerAFPogAPCommandHelper.updateUttaksgradWithTomDateNull
        private fun replaceNullTom(uttaksgrader: List<Uttaksgrad>, tom: LocalDate) {
            uttaksgrader.forEach {
                if (it.tomDato == null) {
                    it.tomDato = fromLocalDate(tom)
                    it.finishInit()
                }
            }
        }

        // SimulerAFPogAPCommandHelper.createUttaksgrad
        private fun uttakGrad(fom: LocalDate?, grad: Int, tom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fromLocalDate(fom)
                uttaksgrad = grad
                tomDato = fromLocalDate(tom)
            }.also { it.finishInit() }
    }
}
