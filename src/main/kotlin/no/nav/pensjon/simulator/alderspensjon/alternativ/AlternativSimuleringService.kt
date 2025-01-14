package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.utkantSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.withLavereUttakGrad
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.exception.AvslagVilkaarsproevingForKortTrygdetidException
import no.nav.pensjon.simulator.core.exception.AvslagVilkaarsproevingForLavtTidligUttakException
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
        foedselsdato: LocalDate,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        return try {
            val lavereGradSpec: SimuleringSpec = withLavereUttakGrad(spec)
            val result: SimulatorOutput = simulator.simuler(lavereGradSpec)
            // Lavere grad innvilget; returner dette som alternativ og avslutt:
            alternativResponse(lavereGradSpec, foedselsdato, pensjon(result))
        } catch (_: AvslagVilkaarsproevingForLavtTidligUttakException) {
            // Lavere grad ga "avslått" resultat; prøv utkanttilfellet og ev. alternative parametre:
            simulerAlternativHvisUtkanttilfelletInnvilges(spec, foedselsdato, inkluderPensjonHvisUbetinget)
        } catch (_: AvslagVilkaarsproevingForKortTrygdetidException) {
            simulerAlternativHvisUtkanttilfelletInnvilges(spec, foedselsdato, inkluderPensjonHvisUbetinget)
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
        foedselsdato: LocalDate,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        val normAlder: Alder = normAlderService.normAlder(foedselsdato)

        return try {
            val utkantSpec: SimuleringSpec = utkantSimuleringSpec(spec, normAlder, foedselsdato)
            simulator.simuler(utkantSpec)
            // resultatet av 'simuler' ignoreres - det interessante er om en exception oppstår

            // Ingen exception => utkanttilfellet innvilget => prøv alternative parametre:
            findAlternativtUttak(spec, foedselsdato, spec.gradertUttak(foedselsdato), spec.heltUttak(foedselsdato))
        } catch (_: AvslagVilkaarsproevingForLavtTidligUttakException) {
            // Utkanttilfellet avslått (intet gradert uttak mulig); returner alternativ for ubetinget uttak:
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normAlder, foedselsdato)
            else
                ubetingetUttakResponseUtenSimulertPensjon(foedselsdato, normAlder)
        } catch (_: AvslagVilkaarsproevingForKortTrygdetidException) {
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normAlder, foedselsdato)
            else
                ubetingetUttakResponseUtenSimulertPensjon(foedselsdato, normAlder)
        }
    }

    private fun ubetingetUttakResponseMedSimulertPensjon(
        spec: SimuleringSpec,
        normAlder: Alder,
        foedselsdato: LocalDate
    ): SimulertPensjonEllerAlternativ =
        try {
            val ubetingetSpec: SimuleringSpec = SimuleringSpecUtil.ubetingetSimuleringSpec(spec, normAlder, foedselsdato)
            val result: SimulatorOutput = simulator.simuler(ubetingetSpec)
            alternativResponse(ubetingetSpec, foedselsdato, pensjon(result))
        } catch (e: AvslagVilkaarsproevingForKortTrygdetidException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        } catch (e: AvslagVilkaarsproevingForLavtTidligUttakException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        }

    private fun ubetingetUttakResponseUtenSimulertPensjon(foedselsdato: LocalDate, normAlder: Alder) =
        alternativResponse(ubetingetUttakAlternativ(foedselsdato, normAlder), alternativPensjon = null)

    private fun findAlternativtUttak(
        spec: SimuleringSpec,
        foedselsdato: LocalDate,
        gradertUttak: GradertUttakSimuleringSpec?,
        heltUttak: HeltUttakSimuleringSpec
    ): SimulertPensjonEllerAlternativ {

        val pensjonEllerAlternativ: SimulertPensjonEllerAlternativ =
            findAlternativtUttak(
                spec,
                foedselsdato,
                heltUttakInntektTomAlderAar = heltUttak.inntektTom.alder.aar,
                foersteUttakAngittAlder = forsteUttakAlder(gradertUttak, heltUttak),
                andreUttakAngittAlder = andreUttakAlder(gradertUttak, heltUttak),
                maxUttaksgrad = gradertUttak?.grad ?: UttakGradKode.P_100
            )

        return pensjonEllerAlternativ.pensjon
            ?.let { pensjonEllerAlternativ } ?: findAlternativFailed()
    }

    private fun findAlternativtUttak(
        spec: SimuleringSpec,
        foedselsdato: LocalDate,
        heltUttakInntektTomAlderAar: Int,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder?, // null if not gradert
        maxUttaksgrad: UttakGradKode
    ): SimulertPensjonEllerAlternativ {
        val normAlder: Alder = normAlderService.normAlder(foedselsdato)
        val foersteUttakMaxAlder = normAlder.minusMaaneder(2)
        val finder =            AlternativtUttakFinder(simulator, spec, normAlderService, heltUttakInntektTomAlderAar)
        val foersteUttakMinAlder = foersteUttakAngittAlder.plusMaaneder(1)
        val andreUttakMinAlder: Alder? =
            andreUttakAngittAlder?.let { if (foersteUttakMinAlder == it) it.plusMaaneder(1) else it }

        val initialResult: SimulertPensjonEllerAlternativ =
            finder.findAlternativtUttak(
                foersteUttakMinAlder,
                foersteUttakMaxAlder,
                andreUttakMinAlder,
                andreUttakMaxAlder = normAlder,
                maxUttaksgrad,
                keepUttaksgradConstant = false
            )

        return initialResult.alternativ?.let {
            if (it.resultStatus == SimulatorResultStatus.SUBOPTIMAL)
                findMoreOptimalUttak(it, finder, foersteUttakAngittAlder, andreUttakAngittAlder) ?: initialResult
            else
                initialResult
        } ?: initialResult
    }

    private fun findMoreOptimalUttak(
        suboptimal: SimulertAlternativ,
        finder: AlternativtUttakFinder,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder?
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
                andreUttakMaxAlder = andreUttakSuboptimalAlder?.alder,
                maxUttaksgrad = suboptimal.uttakGrad,
                keepUttaksgradConstant = true
            )

        return when (betterResult.alternativ?.resultStatus) {
            SimulatorResultStatus.GOOD -> betterResult
            else -> null
        }
    }

    private companion object {

        private fun alternativ(spec: SimuleringSpec, foedselsdato: LocalDate): SimulertAlternativ? =
            spec.gradertUttak(foedselsdato)?.let {
                SimulertAlternativ(
                    gradertUttakAlder = uttakAlder(it.uttakFom.alder, foedselsdato),
                    uttakGrad = it.grad,
                    heltUttakAlder = uttakAlder(spec.heltUttak(foedselsdato).uttakFom.alder, foedselsdato),
                    resultStatus = SimulatorResultStatus.GOOD
                )
            }

        private fun forsteUttakAlder(
            gradertUttak: GradertUttakSimuleringSpec?,
            heltUttak: HeltUttakSimuleringSpec
        ): Alder {
            val alder = (gradertUttak?.uttakFom ?: heltUttak.uttakFom).alder
            return Alder(alder.aar, alder.maaneder)
        }

        private fun andreUttakAlder(
            gradertUttak: GradertUttakSimuleringSpec?,
            heltUttak: HeltUttakSimuleringSpec
        ): Alder? {
            if (gradertUttak == null) {
                return null
            }

            return heltUttak.uttakFom.alder.let { Alder(it.aar, it.maaneder) }
        }

        private fun uttakAlder(alder: Alder, foedselsdato: LocalDate) =
            SimulertUttakAlder(
                alder = Alder(alder.aar, alder.maaneder),
                uttakDato = uttakDato(foedselsdato, alder)
            )

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
            foedselsdato: LocalDate,
            alternativPensjon: SimulertPensjon?
        ) =
            alternativResponse(alternativ(spec, foedselsdato), alternativPensjon)

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
