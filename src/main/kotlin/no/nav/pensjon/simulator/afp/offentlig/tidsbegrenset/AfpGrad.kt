package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.g.GrunnbeloepService
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Beregner AFP-grad basert på inntekt ved AFP-uttak og tidligere inntekt.
 * Tidligere inntekt er snittet av inntekten de tre beste av de fem siste årene før uttak av AFP.
 */
@Component
class AfpGrad(private val grunnbeloepService: GrunnbeloepService) {

    fun beregnAfpGrad(aar: Int, inntektVedAfpUttak: Int, tidligereInntekt: Int): Int {
        val beloepVedAfpUttak = inntektVedAfpUttak.coerceAtLeast(0)
        val tidligereBeloep = tidligereInntekt.coerceAtLeast(0)

        return if (tidligereBeloep <= beloepVedAfpUttak)
            0
        else if (beloepetErStoerreEnnToleransebeloepet(beloepVedAfpUttak, aar))
            (100 - ((beloepVedAfpUttak.toDouble() / tidligereBeloep) * 100)).roundToInt()
        else
            100
    }

    private fun beloepetErStoerreEnnToleransebeloepet(beloep: Int, aar: Int): Boolean =
        beloep > (grunnbeloepVedAaretsStart(aar) * TOLERANSEBELOEP_FAKTOR).roundToInt()

    private fun grunnbeloepVedAaretsStart(aar: Int): Int =
        grunnbeloepService.grunnbeloep(dato = LocalDate.of(aar, 1, 1))

    private companion object {
        /**
         * Fra 2025-01-01 kan man kan tjene inntil et toleransebeløp på 0,26 G i året uten at AFP reduseres.
         * NB: Her antas det at faktoren 0,26 gjelder for alle tjenestepensjonsordninger (kun verifisert for KLP og SPK).
         */
        private const val TOLERANSEBELOEP_FAKTOR = .26
    }
}