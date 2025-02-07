package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import java.time.LocalDate

class AfpOffentligLivsvarigDto(bruttoPerAr: Double) : AfpLivsvarig(
    formelKode = FormelKodeEnum.AFPx,
    bruttoPerAr
) {
    var sistRegulert: LocalDate = LocalDate.now()

    constructor(sistRegulert: LocalDate, bruttoPerAr: Double) : this(bruttoPerAr) {
        this.sistRegulert = sistRegulert
    }

    //TODO sistRegulertG
    fun toVilkarsprovDto() = AfpOffentligLivsvarigGrunnlag(sistRegulertG = 0, bruttoPerAr)

    //Kan sendes uten kovertering til foreldreklassen, naar pensjon-regler publserer et nytt API med stotte til AfpOffentligLivsvarig : AfpLivsvarig
    fun toAfpLivsvarig() =
        AfpLivsvarig().also {
            it.bruttoPerAr = bruttoPerAr
        }
}
