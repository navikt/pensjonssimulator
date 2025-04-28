package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.UttakAlderDiscriminator
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDatoKandidat
import java.time.LocalDate
import kotlin.math.floor

/**
 * Utfører simulering der parameterne utledes fra en heltallsverdi (0-basert).
 * Dette for å tilpasse til binærsøk (algoritmen blir enklere med søk blant heltallsverdier).
 * Heltallsverdien er gitt ved gradertUttakAlderIndex.
 * Dokumentasjon: https://confluence.adeo.no/display/PEN/Simuleringsstrategi+2024
 */
class UfoereIndexBasedSimulering(
    private val discriminator: UttakAlderDiscriminator,
    private val simuleringSpec: SimuleringSpec,
    private val foedselsdato: LocalDate,
    private val andreUttakAlderValueCount: Int,
    private val indexCount: Int,
    private val indexedRelevantUttaksgrader: Map<Int, UttakGradKode>,
    private val foersteUttakAlder: Alder,
    private val andreUttakMinAlder: Alder,
) {
    /**
     * Contains all relevant combinations of uttak parameters;
     * the binary search will pick out some of them and use them in the simulering
     */
    private val indexedUttakSpecs: List<IndexedUttakSpec> = indexedUttakSpecs()

    /**
     * forsteUttakAlderIndex = antall måneder etter laveste alder for første uttak
     * Laveste uttaksalder = Ønsket (men avslått) uttaksalder + 1 måned
     */
    fun tryIndex(index: Int): AlternativSimuleringResult {
        val uttakSpec: IndexedUttakSpec = indexedUttakSpecs[index]
        val alternativSpec: AlternativSimuleringSpec = alternativSpec(uttakSpec)

        return try {
            val currentSimuleringSpec = simuleringSpec.withUttak(
                foersteUttakDato = alternativSpec.gradertUttakFom!!, // never null in this context (uføre, gradert)
                uttaksgrad = alternativSpec.uttakGrad,
                heltUttakDato = alternativSpec.heltUttakFom,
                inntektEtterHeltUttakAntallAar = alternativSpec.inntektEtterHeltUttakAntallAar
            )

            val simulertPensjon: SimulatorOutput = discriminator.simuler(currentSimuleringSpec)

            val uttaksgradTransition: Boolean = index > 0 &&
                    indexedUttakSpecs[index - 1].uttaksgrad.index < uttakSpec.uttaksgrad.index

            AlternativSimuleringResult(
                valueIsGood = true,
                simulertPensjon,
                usedParameters = alternativSpec,
                uttakAlderTransition = uttaksgradTransition //TODO rename uttakAlderTransition
            )
        } catch (_: UtilstrekkeligOpptjeningException) {
            AlternativSimuleringResult(valueIsGood = false, simulertPensjon = null, usedParameters = alternativSpec)
        } catch (_: UtilstrekkeligTrygdetidException) {
            AlternativSimuleringResult(valueIsGood = false, simulertPensjon = null, usedParameters = alternativSpec)
        }
    }

    private fun alternativSpec(uttakSpec: IndexedUttakSpec) =
        AlternativSimuleringSpec(
            gradertUttakFom = uttakSpec.gradertUttakFom.dato,
            gradertUttakAlderIndex = uttakSpec.gradertUttakFom.index,
            uttakGrad = uttakSpec.uttaksgrad.kode,
            heltUttakFom = uttakSpec.heltUttakFom.dato,
            heltUttakAlderIndex = uttakSpec.heltUttakFom.index
        )

    private fun indexedUttakSpecs(): List<IndexedUttakSpec> {
        val gradertUttakFom = IndexedDato(index = 0, dato = uttakDato(foedselsdato, foersteUttakAlder))

        return (0..indexCount - 1).toList().map {
            indexedUttakSpec(index = it, gradertUttakFom)
        }
    }

    /**
     * Spread factor = Factor for 'spreading' a set of integers so that each value is mapped the same number of times
     * to integers in a larger set.
     * E.g. if the task is to map the set [0, 1, 2] to the set [0, 1, 2, 3, 4, 5, 6, 7, 8], then the mapping can be:
     * i = 0 1 2 3 4 5 6 7 8
     * j = 0 0 0 1 1 1 2 2 2
     * This mapping can be expressed as j = floor(i*M/N),
     * where M is the smaller number of values (3) and N is the larger number of values (9).
     * The spread factor = M/N
     */
    private fun indexedUttakSpec(index: Int, gradertUttakFom: IndexedDato) =
        IndexedUttakSpec(
            gradertUttakFom, // constant for uføre

            uttaksgrad = floor(index * indexedRelevantUttaksgrader.size.toDouble() / indexCount).toInt().let {
                IndexedUttaksgrad(index = it, kode = indexedRelevantUttaksgrader[it] ?: UttakGradKode.P_0)
            },

            heltUttakFom = floor(index * andreUttakAlderValueCount.toDouble() / indexCount).toInt().let {
                IndexedDato(index = it, dato = uttakDatoKandidat(foedselsdato, andreUttakMinAlder, it))
            },
        )

    private data class IndexedUttakSpec(
        val gradertUttakFom: IndexedDato,
        val uttaksgrad: IndexedUttaksgrad,
        val heltUttakFom: IndexedDato
    )

    private data class IndexedDato(
        val index: Int,
        val dato: LocalDate
    )

    private data class IndexedUttaksgrad(
        val index: Int,
        val kode: UttakGradKode
    )
}
