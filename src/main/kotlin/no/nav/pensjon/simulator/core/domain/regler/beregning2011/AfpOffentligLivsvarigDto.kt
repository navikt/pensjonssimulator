package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOffentligLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti
import java.time.LocalDate

class AfpOffentligLivsvarigDto(bruttoPerAr: Double) : AfpLivsvarig(
    ytelsekomponentType = YtelsekomponentTypeCti("AFP_OFFENTLIG_LIVSVARIG"),
    formelKode = FormelKodeCti("AFPx"),
    bruttoPerAr = bruttoPerAr,
) {
    var sistRegulert: LocalDate = LocalDate.now()

    constructor(sistRegulert: LocalDate, bruttoPerAr: Double) : this(bruttoPerAr) {
        this.sistRegulert = sistRegulert
    }

    fun toVilkarsprovDto() = AfpOffentligLivsvarig(sistRegulert, bruttoPerAr)

    //Kan sendes uten kovertering til foreldreklassen, naar pensjon-regler publserer et nytt API med stotte til AfpOffentligLivsvarig : AfpLivsvarig
    fun toAfpLivsvarig(): AfpLivsvarig {
        val afpLivsvarig = AfpLivsvarig()
        afpLivsvarig.bruttoPerAr = bruttoPerAr
        return afpLivsvarig
    }
}
