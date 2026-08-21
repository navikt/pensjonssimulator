package no.nav.pensjon.simulator.tid

import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR

object Overlapp {

    fun antallMaaneder(
        aar: Int,
        fom: AarOgMaaned,
        tom: AarOgMaaned
    ): Int =
        when {
            fom.erEtter(tom) || IntRange(fom.aar, tom.aar).contains(aar).not() -> 0
            fom.aar < aar && aar < tom.aar -> MAANEDER_PER_AAR
            fom.aar == aar -> tomMaaned(aar, tom) - fom.maaned + 1
            else -> tom.maaned
        }

    private fun tomMaaned(aar: Int, tom: AarOgMaaned) =
        if (tom.aar == aar) tom.maaned else MAANEDER_PER_AAR
}