package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.UttakAlderDiscriminator
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.uttak.UttakUtil.indexedUttakGradSubmap
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDatoKandidat
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.math.floor

/**
 * Utfører simulering der parameterne utledes fra en heltallsverdi (0-basert).
 * Dette for å tilpasse til binærsøk (algoritmen blir enklere med søk blant heltallsverdier).
 * Heltallsverdien er gitt ved gradertUttakAlderIndex.
 * Dokumentasjon: https://confluence.adeo.no/display/PEN/Simuleringsstrategi+2024
 */
class IndexBasedSimulering(
    private val discriminator: UttakAlderDiscriminator,
    private val simuleringSpec: SimuleringSpec,
    private val foedselsdato: LocalDate,
    foersteUttakAlderValueCount: Int,
    andreUttakAlderValueCount: Int?,
    maxUttaksgrad: UttakGradKode, // høyeste uttaksgrad å ta i betraktning
    keepUttaksgradConstant: Boolean,
    private val foersteUttakMinAlder: Alder,
    private val andreUttakMinAlder: Alder?,
    private val heltUttakInntektTomAlderAar: Int? // behøves bare ved helt uttak når fom/tom angis i form av alder
) {
    private val log = LoggerFactory.getLogger(IndexBasedSimulering::class.java)
    private val gradert = andreUttakAlderValueCount != null
    private val indexedRelevantUttaksgrader = indexRelevantUttakGrader(maxUttaksgrad, keepUttaksgradConstant)
    private val uttaksgradValueCount = indexedRelevantUttaksgrader.size

    // Spread factor = Factor for 'spreading' a set of integers so that each value is mapped the same number of times to integers in a larger set.
    // E.g. if the task is to map the set [0, 1, 2] to the set [0, 1, 2, 3, 4, 5, 6, 7, 8], then the mapping can be:
    // i = 0 1 2 3 4 5 6 7 8
    // j = 0 0 0 1 1 1 2 2 2
    // This mapping can be expressed as j = floor(i*M/N), where M is the smaller number of values (10) and N is the larger number of values (3)
    // The spread factor = M/N
    private val andreUttakAlderSpreadFactor: Double =
        if (gradert)
            andreUttakAlderValueCount!!.toDouble() / foersteUttakAlderValueCount // M/N
        else
            1.0

    private val uttakGradSpreadFactor: Double =
        if (gradert && uttaksgradValueCount < foersteUttakAlderValueCount)
            uttaksgradValueCount.toDouble() / foersteUttakAlderValueCount // M/N
        else
            1.0

    /**
     * forsteUttakAlderIndex = antall måneder etter laveste alder for første uttak
     * Laveste uttaksalder = Ønsket (men avslått) uttaksalder + 1 måned
     */
    fun tryIndex(foersteUttakAlderIndex: Int): AlternativSimuleringResult {
        val uttaksgradIndex: Int = if (gradert) floor(foersteUttakAlderIndex * uttakGradSpreadFactor).toInt() else 0

        val parameters =
            if (gradert)
                gradertUttakSimuleringSpec(foersteUttakAlderIndex, uttaksgradIndex)
            else
                heltUttakSimuleringSpec(foersteUttakAlderIndex)

        return try {
            val indexSimulatorSpec = simuleringSpec.withUttak(
                foersteUttakDato = parameters.gradertUttakFom ?: parameters.heltUttakFom,
                uttaksgrad = parameters.uttakGrad,
                heltUttakDato = parameters.heltUttakFom,
                inntektEtterHeltUttakAntallAar = parameters.inntektEtterHeltUttakAntallAar
            )

            val simulertPensjon: SimulatorOutput = discriminator.simuler(indexSimulatorSpec)
            log.info("Gyldig første uttaksdato - antall måneder: {}", foersteUttakAlderIndex)
            val uttakGradTransition: Boolean =
                gradert && previousUttakGradIndex(foersteUttakAlderIndex) < uttaksgradIndex
            AlternativSimuleringResult(valueIsGood = true, simulertPensjon, parameters, uttakGradTransition)
        } catch (_: UtilstrekkeligTrygdetidException) {
            log.info("Ugyldig første uttaksdato (for kort trygdetid) - antall måneder: $foersteUttakAlderIndex")
            AlternativSimuleringResult(valueIsGood = false, simulertPensjon = null, parameters)
        } catch (_: UtilstrekkeligOpptjeningException) {
            log.info("Ugyldig første uttaksdato (for lavt tidlig uttak) - antall måneder: $foersteUttakAlderIndex")
            AlternativSimuleringResult(valueIsGood = false, simulertPensjon = null, parameters)
            //} catch (e: FunctionalRecoverableException) {
            //    throw SimuleringException("Søk etter første uttaksdato feilet - antall måneder: $forsteUttakAlderIndex", e)
        }
    }

    /**
     * Maps each relevant uttaksgrad to an index (0-based integer)
     */
    private fun indexRelevantUttakGrader(maxGrad: UttakGradKode, useOnlyMaxGrad: Boolean) =
        if (gradert)
            if (useOnlyMaxGrad)
                mapOf(0 to maxGrad)
            else
                indexedUttakGradSubmap(maxGrad)
        else
            mapOf(0 to UttakGradKode.P_100)

    private fun gradertUttakSimuleringSpec(
        gradertUttakAlderIndex: Int,
        uttaksgradIndex: Int
    ): AlternativSimuleringSpec {
        val heltUttakAlderIndex: Int = andreUttakAlderIndex(gradertUttakAlderIndex)
        val gradertUttakFom: LocalDate = uttakDatoKandidat(foedselsdato, foersteUttakMinAlder, gradertUttakAlderIndex)
        val heltUttakFom: LocalDate = uttakDatoKandidat(foedselsdato, andreUttakMinAlder!!, heltUttakAlderIndex)
        val uttaksgrad: UttakGradKode = indexedRelevantUttaksgrader[uttaksgradIndex] ?: UttakGradKode.P_0

        return AlternativSimuleringSpec(
            gradertUttakFom,
            gradertUttakAlderIndex,
            uttaksgrad,
            heltUttakFom,
            heltUttakAlderIndex
        )
    }

    private fun heltUttakSimuleringSpec(heltUttakAlderIndex: Int): AlternativSimuleringSpec {
        val heltUttakFom: LocalDate = uttakDatoKandidat(foedselsdato, foersteUttakMinAlder, heltUttakAlderIndex)
        val uttaksgrad = UttakGradKode.P_100

        val inntektEtterHeltUttakAntallAar =
            if (simuleringSpec.inntektEtterHeltUttakAntallAar == null && heltUttakInntektTomAlderAar != null)
            // antallArInntektEtterHeltUttak avhenger her av heltUttakFom (som har vært ukjent inntil nå)
            // Nå er heltUttakFom kjent, så antallArInntektEtterHeltUttak kan utledes:
                heltUttakInntektTomAlderAar - heltUttakFom.year + 1 // +1 p.g.a. fra/til OG MED
            else
                foedselsdato.year + (simuleringSpec.inntektEtterHeltUttakAntallAar?: 0) - heltUttakFom.year + 1 // +1, siden fra/til OG MED

        return AlternativSimuleringSpec(
            gradertUttakFom = null,
            gradertUttakAlderIndex = null,
            uttaksgrad,
            heltUttakFom,
            heltUttakAlderIndex,
            inntektEtterHeltUttakAntallAar
        )
    }

    private fun andreUttakAlderIndex(foersteUttakAlderIndex: Int): Int =
        if (foersteUttakAlderIndex > 0)
            floor(foersteUttakAlderIndex * andreUttakAlderSpreadFactor).toInt()
        else
            0

    private fun previousUttakGradIndex(foersteUttakAlderIndex: Int): Int =
        if (foersteUttakAlderIndex > 0)
            floor((foersteUttakAlderIndex - 1) * uttakGradSpreadFactor).toInt()
        else
            0
}
