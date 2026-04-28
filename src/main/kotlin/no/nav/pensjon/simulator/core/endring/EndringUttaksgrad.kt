package no.nav.pensjon.simulator.core.endring

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.uttak.Uttaksgrad.HUNDRE_PROSENT
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Uttaksgrader relatert til simulering av endring av alderspensjon.
 */
@Component
class EndringUttaksgrad(private val kravService: KravService) {

    // SimulerEndringAvAPCommand.finnUttaksgradListe
    fun uttaksgradListe(spec: SimuleringSpec, forrigeAlderspensjonKravhodeId: Long?): MutableList<Uttaksgrad> {
        val eksisterendeUttaksgradListe: List<Uttaksgrad> =
            forrigeAlderspensjonKravhodeId?.let(kravService::fetchKravhode)?.uttaksgradListe.orEmpty()

        return newUttaksgradListe(eksisterendeUttaksgradListe, spec)
    }

    private companion object {

        // SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun newUttaksgradListe(
            eksisterendeListe: List<Uttaksgrad>,
            spec: SimuleringSpec
        ): MutableList<Uttaksgrad> {
            val uttaksgrad: Int = spec.uttakGrad.value.toInt()
            val gradert: Boolean = uttaksgrad < HUNDRE_PROSENT.prosentsats
            val foersteUttakFom: LocalDate = spec.foersteUttakDato!!
            val andreUttakFom: LocalDate? = if (gradert) spec.heltUttakDato else null

            return uttaksgraderSomStarterFoerDato(eksisterendeListe, foersteUttakFom).apply {
                add(foersteUttak(fom = foersteUttakFom, uttaksgrad = uttaksgrad, andreUttakFom = andreUttakFom))

                if (gradert) {
                    add(heltUttak(fom = andreUttakFom))
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun uttaksgraderSomStarterFoerDato(
            uttaksgradListe: List<Uttaksgrad>,
            dato: LocalDate
        ): MutableList<Uttaksgrad> {
            val filtrertListe: MutableList<Uttaksgrad> = mutableListOf()

            uttaksgradListe.forEach {
                inkluderHvisFomFoerDato(uttaksgrad = it, dato, targetList = filtrertListe)
            }

            return filtrertListe
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun inkluderHvisFomFoerDato(
            uttaksgrad: Uttaksgrad,
            dato: LocalDate,
            targetList: MutableList<Uttaksgrad>
        ) {
            if (uttaksgrad.fomDatoLd?.isBefore(dato) == true) {
                // NB: A difference from SimulerEndringAvAPCommandHelper is that here the copying of Uttaksgrad
                // is done within the 'if' statement - this avoids unnecessary copying
                uttaksgrad.copy().also {
                    begrensTomDato(grad = it, maxTomDato = dato) // <--- NB: side-effect
                    targetList.add(it)
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun begrensTomDato(grad: Uttaksgrad, maxTomDato: LocalDate) {
            //TODO should this be maxTomDato minus 1 day?
            if (grad.tomDatoLd == null || grad.tomDatoLd!!.isAfter(maxTomDato)) {
                grad.tomDatoLd = maxTomDato.minusDays(1)
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun foersteUttak(
            fom: LocalDate,
            uttaksgrad: Int,
            andreUttakFom: LocalDate?
        ) =
            Uttaksgrad().apply {
                fomDatoLd = fom
                tomDatoLd = andreUttakFom?.minusDays(1)
                this.uttaksgrad = uttaksgrad
            }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun heltUttak(fom: LocalDate?) =
            Uttaksgrad().apply {
                fomDatoLd = fom
                tomDatoLd = null
                uttaksgrad = HUNDRE_PROSENT.prosentsats
            }
    }
}
