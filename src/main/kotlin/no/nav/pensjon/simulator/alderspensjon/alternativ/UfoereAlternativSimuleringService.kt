package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.utkantSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.withGradertInsteadOfHeltUttak
import no.nav.pensjon.simulator.core.spec.SimuleringSpecUtil.withLavereUttakGrad
import no.nav.pensjon.simulator.normalder.NormAlderService
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Utfører simulering med alternative parametre, i den hensikt å finne kombinasjoner som gir "innvilget" i vilkårsprøvingen.
 * Parameterne som varieres er én eller begge av:
 * - Uttaksgrad
 * - Alder for uttak av hel alderspensjon
 * NB: For uføre varieres ikke alder for uttak av gradert alderspensjon (da det kan medføre inntektstap)
 * -------------------------
 * Parameter 'inkluderPensjonHvisUbetinget' er relevant hvis bruker kun kan ta ut pensjon ved normalder (ubetinget alder):
 * - Hvis 'true' vil responsen inkludere simulert pensjon
 * - Hvis 'false' vil responsen bare inneholde informasjon om at bruker kun kan ta ut pensjon ved normalder
 */
@Service
class UfoereAlternativSimuleringService(
    private val simulator: SimulatorCore,
    private val normAlderService: NormAlderService,
    private val alternativtUttakService: UfoereAlternativtUttakService
) {
    fun simulerMedNesteLavereUttaksgrad(spec: SimuleringSpec): SimulertPensjonEllerAlternativ {
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
            simulerAlternativHvisUtkanttilfelletInnvilges(spec) ?: throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            simulerAlternativHvisUtkanttilfelletInnvilges(spec) ?: throw e
        }
    }

    /**
     * Rekursiv funksjon som simulerer med stadig lavere uttaksgrad inntil innvilgelse eller til minste uttaksgrad
     * er nådd. Første uttaksdato holdes konstant.
     * Hvis utgangspunktet er helt uttak, så "konverteres" dette til gradert uttak slik:
     * - uttaksgrad settes til neste lavere verdi
     * - opprinnelig uttaksdato brukes som dato for gradert uttak, og
     * - normert pensjoneringsdato brukes som dato for etterfølgende helt uttak
     */
    fun simulerMedFallendeUttaksgrad(
        spec: SimuleringSpec,
        exception: RuntimeException
    ): SimulertPensjonEllerAlternativ {
        val lavereGradSpec: SimuleringSpec =
            when (spec.uttakGrad) {
                UttakGradKode.P_100 ->
                    withGradertInsteadOfHeltUttak(
                        source = spec,
                        normAlder = normAlderService.normAlder(spec.foedselDato!!),
                        foedselsdato = spec.foedselDato
                    )

                UttakGradKode.P_20 -> throw exception
                UttakGradKode.P_0 -> throw BadSpecException("0 % uttak")

                else -> withLavereUttakGrad(
                    source = spec,
                    tillatOvergangFraHeltTilGradertUttak = true
                )
            }

        return try {
            val result: SimulatorOutput = simulator.simuler(lavereGradSpec)
            // Lavere grad innvilget; returner dette som alternativ og avslutt:
            alternativResponse(
                spec = lavereGradSpec,
                alternativPensjon = if (spec.onlyVilkaarsproeving) null else pensjon(result)
                // for 'onlyVilkaarsproeving' er beregnet pensjon uinteressant (kun vilkårsvurdering blir brukt)
            )
        } catch (e: UtilstrekkeligOpptjeningException) {
            simulerMedFallendeUttaksgrad(lavereGradSpec, e)
        } catch (e: UtilstrekkeligTrygdetidException) {
            simulerMedFallendeUttaksgrad(lavereGradSpec, e)
        }
    }

    /**
     * "Utkanttilfellet" er den "dårligst" mulige kombinasjon av uttaksalder og -grad for gradert uttak.
     * Denne kombinasjonen består av:
     * - Lavest mulig uttaksgrad (20 %)
     * - Angitt alder for uttak av gradert alderspensjon (denne alderen holdes konstant for uføre)
     * - Normalder for uttak av hel alderspensjon (før 2026 er dette 67 år)
     * ---------------
     * Hensikten med å simulere for ytkanttilfellet er å fastslå hvorvidt brukeren kan ta gradert uttak i det hele tatt.
     * Hvis utkantilfellet gir "avslått" i vilkårsprøvingen, kan vi konkludere med at brukeren kun kan ta ut helt uttak,
     * og uttaket kan tidligst starte ved normalderen.
     */
    fun simulerAlternativHvisUtkanttilfelletInnvilges(spec: SimuleringSpec): SimulertPensjonEllerAlternativ? {
        val normAlder: Alder = normAlderService.normAlder(spec.foedselDato!!)

        return try {
            val utkantSpec: SimuleringSpec =
                utkantSimuleringSpec(spec, normAlder, spec.foedselDato, foersteUttakAlderIsConstant = true)

            if (utkantSpec.hasSameUttakAs(spec)) {
                // spec has already resulted in 'avslag', so no point in trying again
                return null
            }

            simulator.simuler(utkantSpec)
            // resultatet av 'simuler' ignoreres - det interessante er om en exception oppstår

            // Ingen exception => utkanttilfellet innvilget => prøv alternative parametre:
            alternativtUttakService.findAlternativtUttak(spec)
        } catch (_: UtilstrekkeligOpptjeningException) {
            // Utkanttilfellet avslått (intet gradert uttak mulig);
            // for uføre er da intet alternativ mulig, siden alder for første uttak ikke skal foreslås økt
            noResult()
        } catch (_: UtilstrekkeligTrygdetidException) {
            noResult()
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

        private fun alternativResponse(spec: SimuleringSpec, alternativPensjon: SimulertPensjon?) =
            alternativResponse(alternativ(spec), alternativPensjon)

        private fun alternativResponse(alternativ: SimulertAlternativ?, alternativPensjon: SimulertPensjon?) =
            SimulertPensjonEllerAlternativ(
                pensjon = alternativPensjon,
                alternativ
            )

        private fun noResult() =
            SimulertPensjonEllerAlternativ(
                pensjon = null,
                alternativ = SimulertAlternativ(
                    gradertUttakAlder = null,
                    uttakGrad = UttakGradKode.P_0,
                    heltUttakAlder = SimulertUttakAlder(alder = Alder(0, 0), uttakDato = LocalDate.MIN),
                    resultStatus = SimulatorResultStatus.NONE
                )
            )
    }
}
