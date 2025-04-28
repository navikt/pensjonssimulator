package no.nav.pensjon.simulator.afp.pre2025

object AfpGrad {
    fun beregnAfpGrad(inntektVedAfpUttak: Int, tidligereInntekt: Int): Int =
        if (tidligereInntekt == 0)
            0
        else
            100 - ((inntektVedAfpUttak.toDouble() / tidligereInntekt) * 100).toInt()
}