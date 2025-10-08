package no.nav.pensjon.simulator.core.out

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpOutput

data class OutputPensjon(
    val alderspensjon: List<OutputAlderspensjon>,
    val alderspensjonFraFolketrygden: List<OutputAlderspensjonFraFolketrygden>,
    val afpPrivat: List<OutputPrivatAfp>,
    val afpOffentligPre2025: OutputPre2025OffentligAfp?,
    val afpOffentlig: List<LivsvarigOffentligAfpOutput>,
    val pensjonBeholdningPeriodeListe: List<OutputPensjonBeholdningPeriode>,
    val harUttak: Boolean,
    val harNokTrygdetidForGarantipensjon: Boolean
)
