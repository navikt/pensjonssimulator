package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import java.time.LocalDate

class AfpYtelseSammenligningsresultat(val fom: LocalDate) {
    var navAfp: Int? = null
    var hentetAfp: Int? = null

    fun afpTallErLike() = navAfp == hentetAfp
}