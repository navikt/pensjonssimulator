package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.utkantSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.withLavereUttakGrad
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
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
    private val normalderService: NormertPensjonsalderService,
    private val alternativtUttakService: AlternativtUttakService
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
                alternativPensjon = if (spec.onlyVilkaarsproeving) null else pensjon(result, spec)
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
        val normalder: Alder = normalderService.normalder(spec.foedselDato!!)

        return try {
            val utkantSpec: SimuleringSpec = utkantSimuleringSpec(spec, normalder, spec.foedselDato)

            if (utkantSpec.hasSameUttakAs(spec)) {
                // spec has already resulted in 'avslag', so no point in trying again
                return null
            }

            simulator.simuler(utkantSpec)
            // resultatet av 'simuler' ignoreres - det interessante er om en exception oppstår

            // Ingen exception => utkanttilfellet innvilget => prøv alternative parametre:
            alternativtUttakService.findAlternativtUttak(
                spec,
                spec.gradertUttak(),
                spec.heltUttak()
            )
        } catch (_: UtilstrekkeligOpptjeningException) {
            // Utkanttilfellet avslått (intet gradert uttak mulig); returner alternativ for ubetinget uttak:
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normalder)
            else
                ubetingetUttakResponseUtenSimulertPensjon(spec.foedselDato, normalder)
        } catch (_: UtilstrekkeligTrygdetidException) {
            if (inkluderPensjonHvisUbetinget)
                ubetingetUttakResponseMedSimulertPensjon(spec, normalder)
            else
                ubetingetUttakResponseUtenSimulertPensjon(spec.foedselDato, normalder)
        }
    }

    private fun ubetingetUttakResponseMedSimulertPensjon(
        spec: SimuleringSpec,
        normalder: Alder
    ): SimulertPensjonEllerAlternativ =
        try {
            val ubetingetSpec: SimuleringSpec = SimuleringSpecUtil.ubetingetSimuleringSpec(spec, normalder)
            val result: SimulatorOutput = simulator.simuler(ubetingetSpec)
            alternativResponse(ubetingetSpec, if (spec.onlyVilkaarsproeving) null else pensjon(result, spec))
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        } catch (e: UtilstrekkeligTrygdetidException) {
            // Skal ikke kunne skje
            throw RuntimeException("Simulering for ubetinget alder feilet", e)
        }

    private fun ubetingetUttakResponseUtenSimulertPensjon(foedselsdato: LocalDate, normalder: Alder) =
        alternativResponse(ubetingetUttakAlternativ(foedselsdato, normalder), alternativPensjon = null)

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

        private fun ubetingetUttakAlternativ(foedselsdato: LocalDate, normalder: Alder) =
            SimulertAlternativ(
                gradertUttakAlder = null,
                uttakGrad = UttakGradKode.P_100,
                heltUttakAlder = ubetingetUttakAlder(foedselsdato, normalder),
                resultStatus = SimulatorResultStatus.GOOD
            )

        private fun ubetingetUttakAlder(foedselsdato: LocalDate, normalder: Alder) =
            SimulertUttakAlder(
                alder = normalder,
                uttakDato = uttakDato(foedselsdato, normalder)
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
    }
}
