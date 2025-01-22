package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import java.time.LocalDate

// PEN: SimulatorSimulertPensjon
data class SimulertPensjon(
    val alderspensjon: List<SimulertAarligAlderspensjon>,
    val alderspensjonFraFolketrygden: List<SimulertAlderspensjonFraFolketrygden>,
    val privatAfp: List<SimulertPrivatAfp>,
    val pre2025OffentligAfp: SimulertPre2025OffentligAfp?,
    val livsvarigOffentligAfp: List<SimulertLivsvarigOffentligAfp>,
    val pensjonBeholdningPeriodeListe: List<SimulertPensjonBeholdningPeriode>,
    val harUttak: Boolean,
    val harNokTrygdetidForGarantipensjon: Boolean,
    val trygdetid: Int,
    val opptjeningGrunnlagListe: List<OpptjeningGrunnlag>
)

data class SimulertAarligAlderspensjon(
    val alderAar: Int,
    val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonBeholdningFoerUttak: Int?
)

data class SimulertAlderspensjonFraFolketrygden(
    val datoFom: LocalDate,
    val delytelseListe: List<SimulertDelytelse>,
    val uttakGrad: Int
)

data class SimulertPrivatAfp(
    val alderAar: Int,
    val beloep: Int
)

data class SimulertPre2025OffentligAfp(
    val alderAar: Int,
    val beloep: Int
)

data class SimulertLivsvarigOffentligAfp(
    val alderAar: Int,
    val beloep: Int
)

data class SimulertPensjonBeholdningPeriode(
    val pensjonBeholdning: Double,
    val garantipensjonBeholdning: Double,
    val garantitilleggBeholdning: Double,
    val datoFom: LocalDate,
    var garantipensjonNivaa: SimulertGarantipensjonNivaa
)

data class SimulertGarantipensjonNivaa(
    val beloep: Double?,
    val satsType: String?,
    val sats: Double?,
    val anvendtTrygdetid: Int?
)

data class SimulertDelytelse(
    val type: YtelseskomponentTypeEnum,
    val beloep: Int
)
