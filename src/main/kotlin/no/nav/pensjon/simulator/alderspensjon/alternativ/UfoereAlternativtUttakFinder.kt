package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.core.UttakAlderDiscriminator
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.search.SmallestValueSearch
import no.nav.pensjon.simulator.uttak.UttakUtil.indexedUttakGradSubmap
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import java.time.LocalDate
import java.time.Period
import kotlin.math.max

/**
 * Gitt at 'ønskede' verdier for uttaksgrad og -alder er angitt, og vilkårsprøvingen av disse gir avslag,
 * så finner denne klassen et alternativt sett av verdier som vilkårsprøvingen godtar.
 * Dokumentasjon: https://confluence.adeo.no/display/PEN/Simuleringsstrategi+2024
 * --------------
 * Denne klassen brukes for uføre som ønsker gradert uttak.
 * Da skal alder for første uttak holdes konstant, da et forslag om økt alder kan innebære tap av inntekt for uføre.
 */
class UfoereAlternativtUttakFinder(
    private val discriminator: UttakAlderDiscriminator,
    private val simuleringSpec: SimuleringSpec,
    private val normAlderService: NormAlderService
) {
    private val foedselsdato: LocalDate by lazy {
        simuleringSpec.pid?.let(discriminator::fetchFoedselsdato) ?: throw InvalidArgumentException("Udefinert PID")
    }

    private val normAlder: Alder by lazy { normAlderService.normAlder(foedselsdato) }

    fun findAlternativtUttak(
        foersteUttakAlder: Alder,
        andreUttakMinAlder: Alder,
        andreUttakMaxAlder: Alder,
        maxUttaksgrad: UttakGradKode,
        keepUttaksgradConstant: Boolean,
    ): SimulertPensjonEllerAlternativ {
        val andreUttakAlderValueCount: Int = andreUttakMaxAlder.antallMaanederEtter(andreUttakMinAlder) + 1
        val indexedRelevantUttaksgrader = indexedRelevantUttaksgrader(maxUttaksgrad, keepUttaksgradConstant)
        val maxIndex: Int = max(andreUttakAlderValueCount, indexedRelevantUttaksgrader.size) - 1

        val simulering = UfoereIndexBasedSimulering(
            discriminator,
            simuleringSpec,
            foedselsdato,
            andreUttakAlderValueCount = andreUttakAlderValueCount.let { if (it < 1) 1 else it },
            indexCount = maxIndex + 1,
            indexedRelevantUttaksgrader,
            foersteUttakAlder,
            andreUttakMinAlder,
        )

        val searchResult: AlternativSimuleringResult? =
            SmallestValueSearch(discriminator = simulering::tryIndex, max = maxIndex).search()

        return pensjonEllerAlternativ(
            usedParameters = searchResult?.usedParameters ?: defaultParameters(),
            pensjon = searchResult?.simulertPensjon,
            resultStatus = searchResult?.let(::resultStatus) ?: SimulatorResultStatus.BAD
        )
    }

    /**
     * Maps each relevant uttaksgrad to an index (0-based integer)
     */
    private fun indexedRelevantUttaksgrader(maxGrad: UttakGradKode, useOnlyMaxGrad: Boolean): Map<Int, UttakGradKode> =
        if (useOnlyMaxGrad)
            mapOf(0 to maxGrad)
        else
            indexedUttakGradSubmap(maxGrad)

    private fun pensjonEllerAlternativ(
        usedParameters: AlternativSimuleringSpec,
        pensjon: SimulatorOutput?,
        resultStatus: SimulatorResultStatus
    ): SimulertPensjonEllerAlternativ {
        val gradertPeriode = usedParameters.gradertUttakFom?.let { periodBetweenFirstDayOfMonth(foedselsdato, it) }
        val helPeriode = periodBetweenFirstDayOfMonth(foedselsdato, usedParameters.heltUttakFom)
        val gradertAlder = gradertPeriode?.let(::alder)
        val helAlder = alder(helPeriode)

        return SimulertPensjonEllerAlternativ(
            pensjon = if (simuleringSpec.onlyVilkaarsproeving) null else pensjon?.let(::pensjon),
            // for 'onlyVilkaarsproeving' er beregnet pensjon uinteressant (kun vilkårsvurdering blir brukt)
            alternativ = SimulertAlternativ(
                uttakGrad = usedParameters.uttakGrad,
                gradertUttakAlder = usedParameters.gradertUttakFom?.let {
                    SimulertUttakAlder(
                        alder = Alder(aar = gradertAlder!!.aar, maaneder = gradertAlder.maaneder),
                        uttakDato = it
                    )
                },
                heltUttakAlder = SimulertUttakAlder(
                    alder = Alder(aar = helAlder.aar, maaneder = helAlder.maaneder),
                    uttakDato = usedParameters.heltUttakFom
                ),
                resultStatus = resultStatus
            )
        )
    }

    /**
     * Default er utkanttilfellet (edge case), dvs. det "dårligst mulige" alternativet for gradert tidliguttak.
     * "Dårligst" vil si minste mulige uttaksgrad (20 %) kombinert med høyest mulig alder (1 måned før normalder).
     */
    private fun defaultParameters() =
        AlternativSimuleringSpec(
            gradertUttakFom = uttakDato(foedselsdato, normAlder.minusMaaneder(1)),
            gradertUttakAlderIndex = null,
            uttakGrad = UttakGradKode.P_20,
            heltUttakFom = uttakDato(foedselsdato, normAlder),
            heltUttakAlderIndex = 0
        )

    private companion object {

        private fun periodBetweenFirstDayOfMonth(a: LocalDate, b: LocalDate) =
            Period.between(a.withDayOfMonth(1), b.withDayOfMonth(1))

        /**
         * Det trekkes fra 1 måned fra perioden for å gi tilsvarende alder.
         * Dette siden periode er beregnet ved å "runde av" startdatoen nedover til første dag i måneden
         * (noe som gjør første måned til en hel måned), mens fødselsdato kan være midt i en måned (noe som ikke gir en hel førstemåned).
         * ---------
         * Eksempel: Fødselsdato = 1964-01-15, dato = 1995-04-01 => alder = 31 år 2 måneder (siden 3. måned ikke er helt fylt)
         *           Periode = fom. 1964-01-01 tom. 1995-04-01 => lengde = 31 år 3 måneder
         */
        private fun alder(period: Period) =
            Alder(
                aar = period.years,
                maaneder = period.months
            ).minusMaaneder(antall = 1)

        private fun resultStatus(result: AlternativSimuleringResult): SimulatorResultStatus =
            if (result.valueIsGood)
                if (result.uttakAlderTransition)
                    SimulatorResultStatus.SUBOPTIMAL
                else
                    SimulatorResultStatus.GOOD
            else
                SimulatorResultStatus.BAD
    }
}
