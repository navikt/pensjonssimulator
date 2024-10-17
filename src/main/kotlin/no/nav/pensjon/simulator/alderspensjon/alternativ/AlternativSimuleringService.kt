package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.utkantSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.withLavereUttakGrad
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.SimulatorFlags
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.SimuleringType
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
    fun simulerMedNesteLavereUttakGrad(
        spec: SimuleringSpec,
        foedselDato: LocalDate,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        return try {
            val lavereGradSpec: SimuleringSpec = withLavereUttakGrad(spec)
            val flags: SimulatorFlags = simulatorFlags(lavereGradSpec)
            val result: SimulatorOutput = simulator.simuler(lavereGradSpec, flags)
            // Lavere grad innvilget; returner dette som alternativ og avslutt:
            alternativResponse(lavereGradSpec, foedselDato, pensjon(result))
        } catch (e: AvslagVilkaarsproevingForLavtTidligUttakException) {
            // Lavere grad ga "avslått" resultat; prøv utkanttilfellet og ev. alternative parametre:
            simulerAlternativHvisUtkanttilfelletInnvilges(spec, foedselDato, inkluderPensjonHvisUbetinget)
        } catch (e: AvslagVilkaarsproevingForKortTrygdetidException) {
            simulerAlternativHvisUtkanttilfelletInnvilges(spec, foedselDato, inkluderPensjonHvisUbetinget)
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
        foedselDato: LocalDate,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        val normAlder: Alder = normAlderService.normAlder(foedselDato)

        return try {
            val utkantSpec: SimuleringSpec = utkantSimuleringSpec(spec, normAlder, foedselDato)
            val flags: SimulatorFlags = simulatorFlags(spec)

            simulator.simuler(utkantSpec, flags)
            // resultatet av 'simuler' ignoreres - det interessante er om en exception oppstår

            // Ingen exception => utkanttilfellet innvilget => prøv alternative parametre:
            findAlternativtUttak(spec, flags, foedselDato, spec.gradertUttak(foedselDato), spec.heltUttak(foedselDato))
        } catch (e: AvslagVilkaarsproevingForLavtTidligUttakException) {
            // Utkanttilfellet avslått (intet gradert uttak mulig); returner alternativ for ubetinget uttak:
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normAlder, foedselDato)
            else
                ubetingetUttakResponseUtenSimulertPensjon(foedselDato, normAlder)
        } catch (e: AvslagVilkaarsproevingForKortTrygdetidException) {
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normAlder, foedselDato)
            else
                ubetingetUttakResponseUtenSimulertPensjon(foedselDato, normAlder)
        }
    }

    private fun ubetingetUttakResponseMedSimulertPensjon(
        spec: SimuleringSpec,
        normAlder: Alder,
        foedselDato: LocalDate
    ): SimulertPensjonEllerAlternativ =
        try {
            val ubetingetSpec: SimuleringSpec = SimuleringSpecUtil.ubetingetSimuleringSpec(spec, normAlder, foedselDato)
            val flags: SimulatorFlags = simulatorFlags(spec)
            val result: SimulatorOutput = simulator.simuler(ubetingetSpec, flags)
            alternativResponse(ubetingetSpec, foedselDato, pensjon(result))
        } catch (e: AvslagVilkaarsproevingForKortTrygdetidException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        } catch (e: AvslagVilkaarsproevingForLavtTidligUttakException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        }

    private fun ubetingetUttakResponseUtenSimulertPensjon(foedselDato: LocalDate, normAlder: Alder) =
        alternativResponse(ubetingetUttakAlternativ(foedselDato, normAlder), alternativPensjon = null)

    private fun findAlternativtUttak(
        spec: SimuleringSpec,
        flags: SimulatorFlags,
        foedselDato: LocalDate,
        gradertUttak: GradertUttakSimuleringSpec?,
        heltUttak: HeltUttakSimuleringSpec
    ): SimulertPensjonEllerAlternativ {

        val pensjonEllerAlternativ: SimulertPensjonEllerAlternativ =
            findAlternativtUttak(
                spec,
                flags,
                foedselDato,
                heltUttakInntektTomAlderAar = heltUttak.inntektTom.alder.aar,
                foersteUttakAngittAlder = forsteUttakAlder(gradertUttak, heltUttak),
                andreUttakAngittAlder = andreUttakAlder(gradertUttak, heltUttak),
                maxUttakGrad = gradertUttak?.grad ?: UttakGradKode.P_100
            )

        return pensjonEllerAlternativ.pensjon
            ?.let { pensjonEllerAlternativ } ?: findAlternativFailed()
    }

    private fun findAlternativtUttak(
        spec: SimuleringSpec,
        flags: SimulatorFlags,
        foedselDato: LocalDate,
        heltUttakInntektTomAlderAar: Int,
        foersteUttakAngittAlder: Alder,
        andreUttakAngittAlder: Alder?, // null if not gradert
        maxUttakGrad: UttakGradKode
    ): SimulertPensjonEllerAlternativ {
        val normAlder: Alder = normAlderService.normAlder(foedselDato)
        val foersteUttakMaxAlder = normAlder.minusMaaneder(2)
        val finder =
            AlternativtUttakFinder(simulator, spec, flags, normAlderService, heltUttakInntektTomAlderAar)
        val foersteUttakMinAlder = foersteUttakAngittAlder.plusMaaneder(1)
        val andreUttakMinAlder: Alder? =
            andreUttakAngittAlder?.let { if (foersteUttakMinAlder == it) it.plusMaaneder(1) else it }

        val initialResult: SimulertPensjonEllerAlternativ =
            finder.findAlternativtUttak(
                foersteUttakMinAlder,
                foersteUttakMaxAlder,
                andreUttakMinAlder,
                andreUttakMaxAlder = normAlder,
                maxUttakGrad,
                keepUttakGradConstant = false
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
                maxUttakGrad = suboptimal.uttakGrad,
                keepUttakGradConstant = true
            )

        return when (betterResult.alternativ?.resultStatus) {
            SimulatorResultStatus.GOOD -> betterResult
            else -> null
        }
    }

    private companion object {

        private fun alternativ(spec: SimuleringSpec, foedselDato: LocalDate): SimulertAlternativ? =
            spec.gradertUttak(foedselDato)?.let {
                SimulertAlternativ(
                    gradertUttakAlder = uttakAlder(it.uttakFom.alder, foedselDato),
                    uttakGrad = it.grad,
                    heltUttakAlder = uttakAlder(spec.heltUttak(foedselDato).uttakFom.alder, foedselDato),
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

        private fun uttakAlder(alder: Alder, foedselDato: LocalDate) =
            SimulertUttakAlder(
                alder = Alder(alder.aar, alder.maaneder),
                uttakDato = uttakDato(foedselDato, alder)
            )

        private fun ubetingetUttakAlternativ(foedselDato: LocalDate, normAlder: Alder) =
            SimulertAlternativ(
                gradertUttakAlder = null,
                uttakGrad = UttakGradKode.P_100,
                heltUttakAlder = ubetingetUttakAlder(foedselDato, normAlder),
                resultStatus = SimulatorResultStatus.GOOD
            )

        private fun ubetingetUttakAlder(foedselDato: LocalDate, normAlder: Alder) =
            SimulertUttakAlder(
                alder = normAlder,
                uttakDato = uttakDato(foedselDato, normAlder)
            )

        private fun alternativResponse(
            spec: SimuleringSpec,
            foedselDato: LocalDate,
            alternativPensjon: SimulertPensjon?
        ) =
            alternativResponse(alternativ(spec, foedselDato), alternativPensjon)

        private fun alternativResponse(alternativ: SimulertAlternativ?, alternativPensjon: SimulertPensjon?) =
            SimulertPensjonEllerAlternativ(
                pensjon = alternativPensjon,
                alternativ
            )

        private fun simulatorFlags(lavereGradSpec: SimuleringSpec) =
            SimulatorFlags(
                inkluderLivsvarigOffentligAfp = lavereGradSpec.type == SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                inkluderPensjonBeholdninger = lavereGradSpec.isHentPensjonsbeholdninger,
                ignoreAvslag = false,
                outputSimulertBeregningInformasjonForAllKnekkpunkter = lavereGradSpec.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter
            )

        private fun findAlternativFailed(): Nothing {
            throw RuntimeException("Failed to find alternative simuleringsparametre")
        }
    }
}
