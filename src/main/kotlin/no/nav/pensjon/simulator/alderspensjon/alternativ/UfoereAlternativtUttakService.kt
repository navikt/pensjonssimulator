package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormAlderService
import org.springframework.stereotype.Service

@Service
class UfoereAlternativtUttakService(
    private val simulator: SimulatorCore,
    private val normAlderService: NormAlderService
) {
    fun findAlternativtUttak(spec: SimuleringSpec): SimulertPensjonEllerAlternativ {
        val gradertUttak = spec.gradertUttak()
        val heltUttak = spec.heltUttak()

        val pensjonEllerAlternativ: SimulertPensjonEllerAlternativ =
            findAlternativtUttak(
                spec,
                foersteUttakAngittAlder = gradertUttak!!.uttakFom.alder, // never null in this context (uføre, gradert)
                andreUttakAngittAlder = heltUttak.uttakFom.alder,
                maxUttaksgrad = gradertUttak.grad
            )

        return if (spec.onlyVilkaarsproeving) // bare vilkårsprøvingresultet ar interessant, ikke beregnet pensjon
            pensjonEllerAlternativ.alternativ
                ?.let { pensjonEllerAlternativ } ?: findAlternativFailed()
        else
            pensjonEllerAlternativ.pensjon
                ?.let { pensjonEllerAlternativ } ?: findAlternativFailed()
    }

    private fun findAlternativtUttak(
        spec: SimuleringSpec,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder,
        maxUttaksgrad: UttakGradKode
    ): SimulertPensjonEllerAlternativ {
        val normAlder: Alder = normAlderService.normAlder(spec.foedselDato) //TODO use foedselDato from PDL, not spec?
        val finder = UfoereAlternativtUttakFinder(simulator, spec, normAlderService)

        val andreUttakMinAlder: Alder? =
            andreUttakAngittAlder.let { if (foersteUttakAngittAlder == it) it.plusMaaneder(1) else it }

        // For 'onlyVilkaarsproeving' (tidligst mulig uttak for tjenestepensjonsordninger) gjelder:
        // (1) Alder for andreuttak er konstant
        // (2) Uttaksgrad er konstant
        // Dermed blir:
        // (1) andreUttakMaxAlder = andreUttakMinAlder
        // (2) keepUttaksgradConstant = true
        val initialResult: SimulertPensjonEllerAlternativ =
            finder.findAlternativtUttak(
                foersteUttakAngittAlder,
                andreUttakMinAlder!!,
                andreUttakMaxAlder = if (spec.onlyVilkaarsproeving) andreUttakMinAlder else normAlder,
                maxUttaksgrad,
                keepUttaksgradConstant = spec.onlyVilkaarsproeving
            )

        return initialResult.alternativ?.let {
            if (it.resultStatus == SimulatorResultStatus.SUBOPTIMAL)
                findMoreOptimalUttak(
                    suboptimal = it,
                    finder,
                    foersteUttakAngittAlder,
                    andreUttakAngittAlder,
                    spec.onlyVilkaarsproeving
                ) ?: initialResult
            else
                initialResult
        } ?: initialResult
    }

    private fun findMoreOptimalUttak(
        suboptimal: SimulertAlternativ,
        finder: UfoereAlternativtUttakFinder,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder,
        onlyVilkaarsproeving: Boolean
    ): SimulertPensjonEllerAlternativ? {
        val betterResult: SimulertPensjonEllerAlternativ =
            finder.findAlternativtUttak(
                foersteUttakAlder = foersteUttakAngittAlder,
                andreUttakMinAlder = andreUttakAngittAlder,
                andreUttakMaxAlder = if (onlyVilkaarsproeving) andreUttakAngittAlder else suboptimal.heltUttakAlder.alder,
                maxUttaksgrad = suboptimal.uttakGrad,
                keepUttaksgradConstant = true
            )

        return when (betterResult.alternativ?.resultStatus) {
            SimulatorResultStatus.GOOD -> betterResult
            else -> null
        }
    }

    private companion object {

        private fun findAlternativFailed(): Nothing {
            throw RuntimeException("Failed to find alternative simuleringsparametre for ufør")
        }
    }
}
