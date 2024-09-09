package no.nav.pensjon.simulator.core.out

data class OutputPensjon(
    val alderspensjon: List<OutputAlderspensjon>,
    val alderspensjonFraFolketrygden: List<OutputAlderspensjonFraFolketrygden>,
    val afpPrivat: List<OutputPrivatAfp>,
    val afpOffentligPre2025: OutputPre2025OffentligAfp?,
    val afpOffentlig: List<OutputLivsvarigOffentligAfp>,
    val pensjonBeholdningPeriodeListe: List<OutputPensjonBeholdningPeriode>,
    val harUttak: Boolean,
    val harNokTrygdetidForGarantipensjon: Boolean
)
