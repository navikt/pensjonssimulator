package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import java.time.LocalDate
import kotlin.math.abs

data class AfpYtelseSammenligningsresultat(val fom: LocalDate) {
    var navAfp: Int? = null
    var hentetAfp: Int? = null

    //ignorer avrundingsforskjeller opptil 10 kroner
    fun afpTallErLike() = abs((navAfp ?: 0) - (hentetAfp ?: 0)) < 11
    override fun toString(): String {
        return "fom=$fom, navAfp=$navAfp, hentetAfp=$hentetAfp"
    }
}