package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import kotlin.math.roundToInt

object AfpGrad {

    /**
     * Fra 2025-01-01 kan man kan tjene inntil et toleransebeløp på 0,26 G i året uten at AFP reduseres.
     * NB: Her antas det at faktoren 0,26 gjelder for alle tjenestepensjonsordninger (kun verifisert for KLP og SPK).
     */
    private const val TOLERANSEBELOEP_FAKTOR = .26

    fun beregnAfpGrad(inntektVedAfpUttak: Int, tidligereInntekt: Int, grunnbeloep: Int): Int =
        if (tidligereInntekt == 0)
            0
        else if (beloepetErStoerreEnnToleransebeloepet(inntektVedAfpUttak, grunnbeloep))
            100 - ((inntektVedAfpUttak.toDouble() / tidligereInntekt) * 100).toInt()
        else
            100

    private fun beloepetErStoerreEnnToleransebeloepet(beloep: Int, grunnbeloep: Int): Boolean =
        beloep > (grunnbeloep * TOLERANSEBELOEP_FAKTOR).roundToInt()
}