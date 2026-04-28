package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.TrygdetidsgrunnlagFactory
object TrygdetidGrunnlagFactory {

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        TTPeriode().apply {
            this.fomLd = fom
            this.tomLd = tom
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = LandkodeEnum.NOR
            this.ikkeProRata = false
            this.bruk = true
        }

    // PEN: SettTrygdetidHelper.createTrygdetidsgrunnlagNorge + TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun trygdetidPeriode(fom: Date, tom: Date?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean?) =
        TTPeriode().apply {
            this.fomLd = fom.toNorwegianLocalDate()
            this.tomLd = tom?.toNorwegianLocalDate()
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = land
            this.ikkeProRata = ikkeProRata
            this.bruk = bruk
        }

    fun trygdetidPeriode(fom: LocalDate, tom: LocalDate?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean?) =
        trygdetidPeriode(fom.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon(), land, ikkeProRata, bruk)
    //TODO: fix

    // PEN: TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun trygdetidPeriode(fom: Date?, tom: Date?, land: LandkodeEnum?) =
        TTPeriode().apply {
            this.fomLd = fom?.toNorwegianLocalDate()
            this.tomLd = tom?.toNorwegianLocalDate()
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = land
            this.ikkeProRata = false
            this.bruk = true
        }

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?, land: LandkodeEnum?) =
        trygdetidPeriode(fom?.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon(), land)

    // PEN: TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlagForenkletSimulering + createTrygdetidsgrunnlag
    fun anonymSimuleringTrygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode().apply {
            this.fomLd = fom?.toNorwegianLocalDate()
            this.tomLd = tom?.toNorwegianLocalDate()
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = LandkodeEnum.NOR
            this.ikkeProRata = true // true for anonym
            this.bruk = true
        }

    fun anonymSimuleringTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        anonymSimuleringTrygdetidPeriode(fom?.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon())
}
