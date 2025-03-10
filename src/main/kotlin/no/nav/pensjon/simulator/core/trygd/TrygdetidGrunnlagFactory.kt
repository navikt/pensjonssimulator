package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate
import java.util.*

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.TrygdetidsgrunnlagFactory
object TrygdetidGrunnlagFactory {

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        trygdetidPeriode(fom?.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon())

    fun trygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode().apply {
            this.fom = fom?.let { Date(it.time) }
            this.tom = tom?.let { Date(it.time) }
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = LandkodeEnum.NOR
            this.ikkeProRata = false
            this.bruk = true
            this.finishInit()
        }

    // SettTrygdetidHelper.createTrygdetidsgrunnlagNorge + TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun trygdetidPeriode(fom: Date, tom: Date?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean?) =
        TTPeriode().apply {
            this.fom = fom
            this.tom = tom
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = land
            this.ikkeProRata = ikkeProRata
            this.bruk = bruk
            this.finishInit()
        }

    fun trygdetidPeriode(fom: LocalDate, tom: LocalDate?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean) =
        trygdetidPeriode(fom.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon(), land, ikkeProRata, bruk)

    // TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun trygdetidPeriode(fom: Date?, tom: Date?, land: LandkodeEnum?) =
        TTPeriode().apply {
            this.fom = fom?.let { Date(it.time) }
            this.tom = tom?.let { Date(it.time) }
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = land
            this.ikkeProRata = false
            this.bruk = true
            this.finishInit()
        }

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?, land: LandkodeEnum) =
        trygdetidPeriode(fom?.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon(), land)

    // TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlagForenkletSimulering + createTrygdetidsgrunnlag
    fun anonymSimuleringTrygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode().apply {
            this.fom = fom?.let { Date(it.time) }
            this.tom = tom?.let { Date(it.time) }
            this.poengIInnAr = false
            this.poengIUtAr = false
            this.landEnum = LandkodeEnum.NOR
            this.ikkeProRata = true // true for anony
            this.bruk = true
            this.finishInit()
        }

    fun anonymSimuleringTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        anonymSimuleringTrygdetidPeriode(fom?.toNorwegianDateAtNoon(), tom?.toNorwegianDateAtNoon())
}
