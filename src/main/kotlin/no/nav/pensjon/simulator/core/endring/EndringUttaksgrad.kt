package no.nav.pensjon.simulator.core.endring

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianNoon
import no.nav.pensjon.simulator.krav.KravService
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
        private const val HELT_UTTAK_GRAD = 100

        // SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun newUttaksgradListe(
            eksisterendeListe: List<Uttaksgrad>,
            spec: SimuleringSpec
        ): MutableList<Uttaksgrad> {
            val uttaksgrad: Int = spec.uttakGrad.value.toInt()
            val gradert: Boolean = uttaksgrad < HELT_UTTAK_GRAD
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
                inkluderHvisFomFoerDato(uttaksgrad = it, dato = dato, targetList = filtrertListe)
            }

            return filtrertListe
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun inkluderHvisFomFoerDato(
            uttaksgrad: Uttaksgrad,
            dato: LocalDate?,
            targetList: MutableList<Uttaksgrad>
        ) {
            if (uttaksgrad.fomDato?.toNorwegianNoon()?.before(dato?.toNorwegianDateAtNoon()) == true) {
                // NB: A difference from SimulerEndringAvAPCommandHelper is that here the copying of Uttaksgrad
                // is done within the 'if' statement - this avoids unnecessary copying
                uttaksgrad.copy().also {
                    begrensTomDato(it, dato) // <--- NB: side-effect
                    targetList.add(it)
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun begrensTomDato(grad: Uttaksgrad, maxTomDato: LocalDate?) {
            if (grad.tomDato == null || grad.tomDato!!.toNorwegianNoon()
                    .after(maxTomDato?.toNorwegianDateAtNoon())) { //TODO should this be maxTom minus 1 day?
                grad.tomDato = maxTomDato?.let { it.minusDays(1)?.toNorwegianDateAtNoon() }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun foersteUttak(
            fom: LocalDate,
            uttaksgrad: Int,
            andreUttakFom: LocalDate?
        ) =
            Uttaksgrad().apply {
                fomDato = fom.toNorwegianDateAtNoon()
                tomDato = andreUttakFom?.minusDays(1)?.toNorwegianDateAtNoon()
                this.uttaksgrad = uttaksgrad
            }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun heltUttak(fom: LocalDate?) =
            Uttaksgrad().apply {
                fomDato = fom?.toNorwegianDateAtNoon()
                tomDato = null
                uttaksgrad = HELT_UTTAK_GRAD
            }
    }
}
