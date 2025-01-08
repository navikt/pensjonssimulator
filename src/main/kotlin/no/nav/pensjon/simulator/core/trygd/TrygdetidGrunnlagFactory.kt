package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import java.time.LocalDate
import java.util.Date

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.TrygdetidsgrunnlagFactory
object TrygdetidGrunnlagFactory {

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        trygdetidPeriode(fromLocalDate(fom), fromLocalDate(tom))

    fun trygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode(
            fom = fom?.let { Date(it.time) },
            tom = tom?.let { Date(it.time) },
            poengIInnAr = false,
            poengIUtAr = false,
            land = LandCti(LandkodeEnum.NOR.name),
            ikkeProRata = false,
            bruk = true,
        ).also { it.finishInit() }

    // SettTrygdetidHelper.createTrygdetidsgrunnlagNorge + TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun trygdetidPeriode(fom: Date, tom: Date?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean?) =
        TTPeriode(
            fom = fom,
            tom = tom,
            poengIInnAr = false,
            poengIUtAr = false,
            land = land?.let { LandCti(it.name) },
            ikkeProRata = ikkeProRata,
            bruk = bruk
        ).also { it.finishInit() }

    fun trygdetidPeriode(fom: LocalDate, tom: LocalDate?, land: LandkodeEnum?, ikkeProRata: Boolean, bruk: Boolean) =
        trygdetidPeriode(fromLocalDate(fom)!!, fromLocalDate(tom), land, ikkeProRata, bruk)

    // TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun trygdetidPeriode(fom: Date?, tom: Date?, land: LandkodeEnum?) =
        TTPeriode(
            fom = fom?.let { Date(it.time) },
            tom = tom?.let { Date(it.time) },
            poengIInnAr = false,
            poengIUtAr = false,
            land = land?.let { LandCti(it.name) },
            ikkeProRata = false,
            bruk = true
        ).also { it.finishInit() }

    fun trygdetidPeriode(fom: LocalDate?, tom: LocalDate?, land: LandkodeEnum) =
        trygdetidPeriode(fromLocalDate(fom), fromLocalDate(tom), land)

    // TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlagForenkletSimulering + createTrygdetidsgrunnlag
    fun anonymSimuleringTrygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode(
            fom = fom?.let { Date(it.time) },
            tom = tom?.let { Date(it.time) },
            poengIInnAr = false,
            poengIUtAr = false,
            land = LandCti(LandkodeEnum.NOR.name),
            ikkeProRata = true, // true for anonym
            bruk = true
        ).also { it.finishInit() }

    fun anonymSimuleringTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        anonymSimuleringTrygdetidPeriode(fromLocalDate(fom), fromLocalDate(tom))
}
