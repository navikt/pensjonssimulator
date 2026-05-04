package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.TrygdetidsgrunnlagFactory
object TrygdetidGrunnlagFactory {

    fun trygdetidPeriode(fom: LocalDate, tom: LocalDate?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean?) =
        TTPeriode().apply {
            this.fomLd = fom
            this.tomLd = tom
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = land
            this.ikkeProRata = ikkeProRata
            this.bruk = bruk
        }

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?, land: LandkodeEnum? = LandkodeEnum.NOR) =
        TTPeriode().apply {
            this.fomLd = fom
            this.tomLd = tom
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = land
            this.ikkeProRata = false
            this.bruk = true
        }

    fun anonymSimuleringTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        TTPeriode().apply {
            this.fomLd = fom
            this.tomLd = tom
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = LandkodeEnum.NOR
            this.ikkeProRata = true // true for anonym
            this.bruk = true
        }
}
