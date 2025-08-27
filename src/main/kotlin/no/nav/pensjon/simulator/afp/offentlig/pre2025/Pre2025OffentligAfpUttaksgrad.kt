package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
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
            return mutableListOf(uttaksgrad(fom = ubetingetUttakDato, grad = 100, tom = null))
        }

        val eksisterendeKravhode: Kravhode? =
            forrigeAlderspensjonBeregningResultat.kravId?.let(kravService::fetchKravhode)

        val uttaksgradListe: MutableList<Uttaksgrad> = mutableListOf()
        uttaksgradListe.addAll(eksisterendeKravhode?.uttaksgradListe.orEmpty().map { it.copy() })
        spec.foersteUttakDato?.let { replaceNullTom(uttaksgradListe, it.minusDays(1)) }

        val foersteTom: LocalDate = ubetingetUttakDato.minusDays(1)
        uttaksgradListe.add(uttaksgrad(fom = spec.foersteUttakDato, grad = 0, tom = foersteTom))
        uttaksgradListe.add(uttaksgrad(fom = ubetingetUttakDato, grad = 100, tom = null))
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

        // PEN: SimulerAFPogAPCommandHelper.updateUttaksgradWithTomDateNull
        private fun replaceNullTom(uttaksgradListe: List<Uttaksgrad>, tom: LocalDate) {
            uttaksgradListe.forEach {
                if (it.tomDato == null) {
                    it.tomDato = tom.toNorwegianDateAtNoon()
                    it.setDatesToNoon()
                }
            }
        }

        // PEN: SimulerAFPogAPCommandHelper.createUttaksgrad
        private fun uttaksgrad(fom: LocalDate?, grad: Int, tom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fom?.toNorwegianDateAtNoon()
                tomDato = tom?.toNorwegianDateAtNoon()
                uttaksgrad = grad
            }
    }
}
