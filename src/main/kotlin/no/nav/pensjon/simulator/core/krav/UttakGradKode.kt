package no.nav.pensjon.simulator.core.krav

//TODO replace by no.nav.pensjon.simulator.core.domain.regler.enum.UttaksgradEnum?
// This enum is 1:1 with no.nav.domain.pensjon.kjerne.kodetabeller.UttaksgradCode
enum class UttakGradKode(val value: String) {
    /**
     * 0 %
     */
    P_0("0"),

    /**
     * 100 %
     */
    P_100("100"),

    /**
     * 20 %
     */
    P_20("20"),

    /**
     * 40 %
     */
    P_40("40"),

    /**
     * 50 %
     */
    P_50("50"),

    /**
     * 60 %
     */
    P_60("60"),

    /**
     * 80 %
     */
    P_80("80")
}
