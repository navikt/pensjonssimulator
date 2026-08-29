package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.core.krav.UttakGradKode

object SimuleringSpecUtil {

    fun naermesteLavereUttaksgrad(grad: UttakGradKode) =
        when (grad) {
            UttakGradKode.P_0 -> UttakGradKode.P_0
            UttakGradKode.P_20 -> UttakGradKode.P_0
            UttakGradKode.P_40 -> UttakGradKode.P_20
            UttakGradKode.P_50 -> UttakGradKode.P_40
            UttakGradKode.P_60 -> UttakGradKode.P_50
            UttakGradKode.P_80 -> UttakGradKode.P_60
            UttakGradKode.P_100 -> UttakGradKode.P_80
        }
}