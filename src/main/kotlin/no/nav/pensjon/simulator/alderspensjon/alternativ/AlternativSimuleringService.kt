package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.utkantSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.withLavereUttakGrad
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.GradertUttakSimuleringSpec
import no.nav.pensjon.simulator.core.spec.HeltUttakSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Utfører simulering med alternative parametre, i den hensikt å finne kombinasjoner som gir "innvilget" i vilkårsprøvingen.
 * Parameterne som varieres er én eller flere av:
 * - Uttaksgrad
 * - Alder for uttak av gradert alderspensjon
 * - Alder for uttak av hel alderspensjon
 * -------------------------
 * Parameter 'inkluderPensjonHvisUbetinget' er relevant hvis bruker kun kan ta ut pensjon ved normalder (ubetinget alder):
 * - Hvis 'true' vil responsen inkludere simulert pensjon
 * - Hvis 'false' vil responsen bare inneholde informasjon om at bruker kun kan ta ut pensjon ved normalder
 */
@Service
class AlternativSimuleringService(
    private val simulator: SimulatorCore,
    private val normAlderService: NormAlderService
) {
    fun simulerMedNesteLavereUttaksgrad(
        spec: SimuleringSpec,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        return try {
            val lavereGradSpec: SimuleringSpec = withLavereUttakGrad(spec)
            val result: SimulatorOutput = simulator.simuler(lavereGradSpec)
            // Lavere grad innvilget; returner dette som alternativ og avslutt:
            alternativResponse(
                spec = lavereGradSpec,
                alternativPensjon = if (spec.onlyVilkaarsproeving) null else pensjon(result)
                // for 'onlyVilkaarsproeving' er beregnet pensjon uinteressant (kun vilkårsvurdering blir brukt)
            )
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Lavere grad ga "avslått" resultat; prøv utkanttilfellet og ev. alternative parametre:
            simulerAlternativHvisUtkanttilfelletInnvilges(spec, inkluderPensjonHvisUbetinget) ?: throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            simulerAlternativHvisUtkanttilfelletInnvilges(spec, inkluderPensjonHvisUbetinget) ?: throw e
        }
    }

    /**
     * "Utkanttilfellet" er den "dårligst" mulige kombinasjon av uttaksalder og -grad for gradert uttak.
     * Denne kombinasjonen består av:
     * - Lavest mulig uttaksgrad (20 %)
     * - Høyest mulig alder for uttak av gradert alderspensjon (én måned før normalder)
     * - Normalder for uttak av hel alderspensjon (før 2026 er dette 67 år)
     * ---------------
     * Hensikten med å simulere for ytkanttilfellet er å fastslå hvorvidt brukeren kan ta gradert uttak i det hele tatt.
     * Hvis utkantilfellet gir "avslått" i vilkårsprøvingen, kan vi konkludere med at brukeren kun kan ta ut helt uttak,
     * og uttaket kan tidligst starte ved normalderen.
     */
    fun simulerAlternativHvisUtkanttilfelletInnvilges(
        spec: SimuleringSpec,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ? {
        val normAlder: Alder = normAlderService.normAlder(spec.foedselDato)

        return try {
            val utkantSpec: SimuleringSpec = utkantSimuleringSpec(spec, normAlder, spec.foedselDato!!)

            if (utkantSpec.hasSameUttakAs(spec)) {
                // spec has already resulted in 'avslag', so no point in trying again
                return null
            }

            simulator.simuler(utkantSpec)
            // resultatet av 'simuler' ignoreres - det interessante er om en exception oppstår

            // Ingen exception => utkanttilfellet innvilget => prøv alternative parametre:
            findAlternativtUttak(
                spec,
                spec.gradertUttak(),
                spec.heltUttak()
            )
        } catch (_: UtilstrekkeligOpptjeningException) {
            // Utkanttilfellet avslått (intet gradert uttak mulig); returner alternativ for ubetinget uttak:
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normAlder)
            else
                ubetingetUttakResponseUtenSimulertPensjon(spec.foedselDato!!, normAlder)
        } catch (_: UtilstrekkeligTrygdetidException) {
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normAlder)
            else
                ubetingetUttakResponseUtenSimulertPensjon(spec.foedselDato!!, normAlder)
        }
    }

    private fun ubetingetUttakResponseMedSimulertPensjon(
        spec: SimuleringSpec,
        normAlder: Alder
    ): SimulertPensjonEllerAlternativ =
        try {
            val ubetingetSpec: SimuleringSpec = SimuleringSpecUtil.ubetingetSimuleringSpec(spec, normAlder)
            val result: SimulatorOutput = simulator.simuler(ubetingetSpec)
            alternativResponse(ubetingetSpec, if (spec.onlyVilkaarsproeving) null else pensjon(result))
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        } catch (e: UtilstrekkeligTrygdetidException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        }

    private fun ubetingetUttakResponseUtenSimulertPensjon(foedselsdato: LocalDate, normAlder: Alder) =
        alternativResponse(ubetingetUttakAlternativ(foedselsdato, normAlder), alternativPensjon = null)

    private fun findAlternativtUttak(
        spec: SimuleringSpec,
        gradertUttak: GradertUttakSimuleringSpec?,
        heltUttak: HeltUttakSimuleringSpec
    ): SimulertPensjonEllerAlternativ {

        val pensjonEllerAlternativ: SimulertPensjonEllerAlternativ =
            findAlternativtUttak(
                spec,
                heltUttakInntektTomAlderAar = heltUttak.inntektTom.alder.aar,
                foersteUttakAngittAlder = forsteUttakAlder(gradertUttak, heltUttak),
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
        val normAlder: Alder = normAlderService.normAlder(spec.foedselDato)
        val finder = AlternativtUttakFinder(simulator, spec, normAlderService, heltUttakInntektTomAlderAar)
        val foersteUttakMinAlder = foersteUttakAngittAlder.plusMaaneder(1)

        val andreUttakMinAlder: Alder? =
            andreUttakAngittAlder?.let { if (foersteUttakMinAlder == it) it.plusMaaneder(1) else it }

        val foersteUttakMaxAlder: Alder =
            if (spec.onlyVilkaarsproeving && spec.isGradert())
                andreUttakMinAlder!!.minusMaaneder(1)
            else
                normAlder.minusMaaneder(2)

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
                andreUttakMaxAlder = if (spec.onlyVilkaarsproeving) andreUttakMinAlder else normAlder,
                maxUttaksgrad,
                keepUttaksgradConstant = spec.onlyVilkaarsproeving
            )

        return initialResult.alternativ?.let {
            if (it.resultStatus == SimulatorResultStatus.SUBOPTIMAL)
                findMoreOptimalUttak(
                    it,
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

        private fun alternativ(spec: SimuleringSpec): SimulertAlternativ? =
            spec.gradertUttak()?.let {
                val heltUttakFom = spec.heltUttak().uttakFom

                SimulertAlternativ(
                    gradertUttakAlder = SimulertUttakAlder(it.uttakFom.alder, it.uttakFom.dato),
                    uttakGrad = it.grad,
                    heltUttakAlder = SimulertUttakAlder(heltUttakFom.alder, heltUttakFom.dato),
                    resultStatus = SimulatorResultStatus.GOOD
                )
            }

        private fun forsteUttakAlder(
            gradertUttak: GradertUttakSimuleringSpec?,
            heltUttak: HeltUttakSimuleringSpec
        ): Alder =
            (gradertUttak?.uttakFom ?: heltUttak.uttakFom).alder

        private fun andreUttakAlder(
            gradertUttak: GradertUttakSimuleringSpec?,
            heltUttak: HeltUttakSimuleringSpec
        ): Alder? {
            if (gradertUttak == null) {
                return null
            }

            return heltUttak.uttakFom.alder
        }

        private fun ubetingetUttakAlternativ(foedselsdato: LocalDate, normAlder: Alder) =
            SimulertAlternativ(
                gradertUttakAlder = null,
                uttakGrad = UttakGradKode.P_100,
                heltUttakAlder = ubetingetUttakAlder(foedselsdato, normAlder),
                resultStatus = SimulatorResultStatus.GOOD
            )

        private fun ubetingetUttakAlder(foedselsdato: LocalDate, normAlder: Alder) =
            SimulertUttakAlder(
                alder = normAlder,
                uttakDato = uttakDato(foedselsdato, normAlder)
            )

        private fun alternativResponse(
            spec: SimuleringSpec,
            alternativPensjon: SimulertPensjon?
        ) =
            alternativResponse(alternativ(spec), alternativPensjon)

        private fun alternativResponse(alternativ: SimulertAlternativ?, alternativPensjon: SimulertPensjon?) =
            SimulertPensjonEllerAlternativ(
                pensjon = alternativPensjon,
                alternativ
            )

        private fun findAlternativFailed(): Nothing {
            throw RuntimeException("Failed to find alternative simuleringsparametre")
        }
    }
}
