package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import java.time.LocalDate
import java.util.Date

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.TrygdetidsgrunnlagFactory
object TrygdetidGrunnlagFactory {

    fun newTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        newTrygdetidPeriode(fromLocalDate(fom), fromLocalDate(tom))

    fun newTrygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode(
            fom = fom?.let { Date(it.time) },
            tom = tom?.let { Date(it.time) },
            poengIInnAr = false,
            poengIUtAr = false,
            land = LandCti(Land.NOR.name),
            ikkeProRata = false,
            bruk = true,
        ).also { it.finishInit() }

    // SettTrygdetidHelper.createTrygdetidsgrunnlagNorge + TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun newTrygdetidPeriode(fom: Date, tom: Date?, land: Land, ikkeProRata: Boolean, bruk: Boolean) =
        TTPeriode(
            fom = fom,
            tom = tom,
            poengIInnAr = false,
            poengIUtAr = false,
            land = LandCti(land.name),
            ikkeProRata = ikkeProRata,
            bruk = bruk
        ).also { it.finishInit() }

    fun newTrygdetidPeriode(fom: LocalDate, tom: LocalDate?, land: Land, ikkeProRata: Boolean, bruk: Boolean) =
        newTrygdetidPeriode(fromLocalDate(fom)!!, fromLocalDate(tom), land, ikkeProRata, bruk)

    // TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlag
    fun newTrygdetidPeriode(fom: Date?, tom: Date?, land: Land) =
        TTPeriode(
            fom = fom?.let { Date(it.time) },
            tom = tom?.let { Date(it.time) },
            poengIInnAr = false,
            poengIUtAr = false,
            land = LandCti(land.name),
            ikkeProRata = false,
            bruk = true
        ).also { it.finishInit() }

    fun newTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?, land: Land) =
        newTrygdetidPeriode(fromLocalDate(fom), fromLocalDate(tom), land)

    // TrygdetidsgrunnlagFactory.createTrygdetidsgrunnlagForenkletSimulering + createTrygdetidsgrunnlag
    fun anonymSimuleringTrygdetidPeriode(fom: Date?, tom: Date?) =
        TTPeriode(
            fom = fom?.let { Date(it.time) },
            tom = tom?.let { Date(it.time) },
            poengIInnAr = false,
            poengIUtAr = false,
            land = LandCti(Land.NOR.name),
            ikkeProRata = true, // true for forenklet
            bruk = true
        ).also { it.finishInit() }

    fun anonymSimuleringTrygdetidPeriode(fom: LocalDate?, tom: LocalDate?) =
        anonymSimuleringTrygdetidPeriode(fromLocalDate(fom), fromLocalDate(tom))
}
