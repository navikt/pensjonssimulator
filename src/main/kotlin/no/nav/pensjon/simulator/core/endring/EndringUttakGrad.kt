package no.nav.pensjon.simulator.core.endring

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianNoon
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Controller
import java.time.LocalDate

/**
 * Uttaksgrader relatert til simulering av endring av alderspensjon.
 */
@Controller
class EndringUttakGrad(private val kravService: KravService) {

    // SimulerEndringAvAPCommand.finnUttaksgradListe
    fun uttakGradListe(spec: SimuleringSpec, forrigeAlderspensjonKravhodeId: Long?): MutableList<Uttaksgrad> {
        val eksisterendeUttakGradListe: List<Uttaksgrad> =
            forrigeAlderspensjonKravhodeId?.let(kravService::fetchKravhode)?.uttaksgradListe.orEmpty()

        return newUttakGradListe(eksisterendeUttakGradListe, spec)
    }

    private companion object {
        private const val HELT_UTTAK_GRAD = 100

        // SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun newUttakGradListe(
            eksisterendeListe: List<Uttaksgrad>,
            spec: SimuleringSpec
        ): MutableList<Uttaksgrad> {
            val uttakGrad: Int = spec.uttakGrad.value.toInt()
            val gradert: Boolean = uttakGrad < HELT_UTTAK_GRAD
            val foersteUttakFom: LocalDate = spec.foersteUttakDato!!
            val andreUttakFom: LocalDate? = if (gradert) spec.heltUttakDato else null

            return uttakGraderSomStarterFoerDato(eksisterendeListe, foersteUttakFom).apply {
                add(foersteUttak(fom = foersteUttakFom, uttaksgrad = uttakGrad, andreUttakFom = andreUttakFom))

                if (gradert) {
                    add(heltUttak(fom = andreUttakFom))
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun uttakGraderSomStarterFoerDato(
            uttakGradListe: List<Uttaksgrad>,
            dato: LocalDate
        ): MutableList<Uttaksgrad> {
            val filtrertListe: MutableList<Uttaksgrad> = mutableListOf()

            uttakGradListe.forEach {
                inkluderHvisFomFoerDato(uttakGrad = it, dato = dato, targetList = filtrertListe)
            }

            return filtrertListe
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun inkluderHvisFomFoerDato(uttakGrad: Uttaksgrad, dato: LocalDate?, targetList: MutableList<Uttaksgrad>) {
            if (uttakGrad.fomDato?.toNorwegianNoon()?.before(dato?.toNorwegianDateAtNoon()) == true) {
                // NB: A difference from SimulerEndringAvAPCommandHelper is that here the copying of Uttaksgrad
                // is done within the 'if' statement - this avoids unnecessary copying
                Uttaksgrad(uttakGrad).also {
                    begrensTomDato(it, dato) // <--- NB: side-effect
                    targetList.add(it)
                }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun begrensTomDato(grad: Uttaksgrad, maxTomDato: LocalDate?) {
            if (grad.tomDato == null || grad.tomDato!!.toNorwegianNoon().after(maxTomDato?.toNorwegianDateAtNoon())) { //TODO should this be maxTom minus 1 day?
                grad.tomDato = maxTomDato?.let { it.minusDays(1)?.toNorwegianDateAtNoon() }
            }
        }

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun foersteUttak(
            fom: LocalDate,
            uttaksgrad: Int,
            andreUttakFom: LocalDate?
        ) = Uttaksgrad(
            fomDato = fom.toNorwegianDateAtNoon(),
            tomDato = andreUttakFom?.minusDays(1)?.toNorwegianDateAtNoon(),
            uttaksgrad
        )

        // Extracted from SimulerEndringAvAPCommandHelper.createUttaksgradListe
        private fun heltUttak(fom: LocalDate?) =
            Uttaksgrad(
                fomDato = fom?.toNorwegianDateAtNoon(),
                tomDato = null,
                uttaksgrad = HELT_UTTAK_GRAD
            )
    }
}
