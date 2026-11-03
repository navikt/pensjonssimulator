package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import java.time.LocalDate

data class AfpYtelseSammenligningsresultat(val fom: LocalDate) {
    var navAfp: Int? = null
    var hentetAfp: Int? = null

    fun afpTallErLike() = navAfp == hentetAfp
    override fun toString(): String {
        return "fom=$fom, navAfp=$navAfp, hentetAfp=$hentetAfp"
    }
}