package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.GradertUttakSimuleringSpec
import no.nav.pensjon.simulator.core.spec.HeltUttakSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service

@Service
class AlternativtUttakService(
    private val simulator: SimulatorCore,
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun findAlternativtUttak(
        spec: SimuleringSpec,
        gradertUttak: GradertUttakSimuleringSpec?,
        heltUttak: HeltUttakSimuleringSpec
    ): SimulertPensjonEllerAlternativ {

        val pensjonEllerAlternativ: SimulertPensjonEllerAlternativ =
            findAlternativtUttak(
                spec,
                heltUttakInntektTomAlderAar = heltUttak.inntektTom.alder.aar,
                foersteUttakAngittAlder = foersteUttakAlder(gradertUttak, heltUttak),
                andreUttakAngittAlder = andreUttakAlder(gradertUttak, heltUttak),
                maxUttaksgrad = gradertUttak?.grad ?: UttakGradKode.P_100
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
        heltUttakInntektTomAlderAar: Int,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder?, // null if not gradert
        maxUttaksgrad: UttakGradKode
    ): SimulertPensjonEllerAlternativ {
        val normalder: Alder = normalderService.normalder(spec.foedselDato!!)
        val finder = AlternativtUttakFinder(simulator, spec, normalderService, heltUttakInntektTomAlderAar, time)
        val foersteUttakMinAlder = foersteUttakAngittAlder.plusMaaneder(1)

        val andreUttakMinAlder: Alder? =
            andreUttakAngittAlder?.let { if (foersteUttakMinAlder == it) it.plusMaaneder(1) else it }

        val foersteUttakMaxAlder: Alder =
            if (spec.onlyVilkaarsproeving && spec.isGradert())
                andreUttakMinAlder!!.minusMaaneder(1)
            else
                normalder.minusMaaneder(2)

        // For 'onlyVilkaarsproeving' (tidligst mulig uttak for tjenestepensjonsordninger) gjelder:
        // (1) Alder for andreuttak er konstant
        // (2) Uttaksgrad er konstant
        // Dermed blir:
        // (1) andreUttakMaxAlder = andreUttakMinAlder
        // (2) keepUttaksgradConstant = true
        val initialResult: SimulertPensjonEllerAlternativ =
            finder.findAlternativtUttak(
                foersteUttakMinAlder,
                foersteUttakMaxAlder,
                andreUttakMinAlder,
                andreUttakMaxAlder = if (spec.onlyVilkaarsproeving) andreUttakMinAlder else normalder,
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
        finder: AlternativtUttakFinder,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder?,
        onlyVilkaarsproeving: Boolean
    ): SimulertPensjonEllerAlternativ? {
        val gradertUttakAlder: SimulertUttakAlder? = suboptimal.gradertUttakAlder
        val heltUttakAlder: SimulertUttakAlder = suboptimal.heltUttakAlder
        val foersteUttakSuboptimalAlder: SimulertUttakAlder = gradertUttakAlder ?: heltUttakAlder

        val andreUttakSuboptimalAlder: SimulertUttakAlder? =
            if (gradertUttakAlder == null) null else heltUttakAlder

        val betterResult: SimulertPensjonEllerAlternativ =
            finder.findAlternativtUttak(
                foersteUttakMinAlder = foersteUttakAngittAlder,
                foersteUttakMaxAlder = foersteUttakSuboptimalAlder.alder.minusMaaneder(1),
                andreUttakMinAlder = andreUttakAngittAlder,
                andreUttakMaxAlder = if (onlyVilkaarsproeving) andreUttakAngittAlder else andreUttakSuboptimalAlder?.alder,
                maxUttaksgrad = suboptimal.uttakGrad,
                keepUttaksgradConstant = true
            )

        return when (betterResult.alternativ?.resultStatus) {
            SimulatorResultStatus.GOOD -> betterResult
            else -> null
        }
    }

    private companion object {

        private fun foersteUttakAlder(
            gradertUttak: GradertUttakSimuleringSpec?,
            heltUttak: HeltUttakSimuleringSpec
        ): Alder =
            (gradertUttak?.uttakFom ?: heltUttak.uttakFom).alder

        private fun andreUttakAlder(
            gradertUttak: GradertUttakSimuleringSpec?,
            heltUttak: HeltUttakSimuleringSpec
        ): Alder? =
            if (gradertUttak == null) null
            else heltUttak.uttakFom.alder

        private fun findAlternativFailed(): Nothing {
            throw RuntimeException("Failed to find alternative simuleringsparametre")
        }
    }
}
