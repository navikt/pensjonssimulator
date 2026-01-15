package no.nav.pensjon.simulator.hybrid

import no.nav.pensjon.simulator.uttak.Uttaksgrad
import no.nav.pensjon.simulator.validity.Problem

data class AlderspensjonOgPrivatAfpResult(
    val suksess: Boolean,
    val alderspensjonsperiodeListe: List<Alderspensjonsperiode>,
    val privatAfpPeriodeListe: List<PrivatAfpPeriode>,
    val harNaavaerendeUttak: Boolean,
    val harTidligereUttak: Boolean,
    val harLoependePrivatAfp: Boolean,
    val problem: Problem? = null
)

data class Alderspensjonsperiode(
    val alderAar: Int,
    val beloep: Int,
    val fom: String,
    val uttaksperiodeListe: List<Uttaksperiode>
)

data class PrivatAfpPeriode(
    val alderAar: Int,
    val beloep: Int
)

data class Uttaksperiode(
    val startmaaned: Int,
    val uttaksgrad: Uttaksgrad
)
