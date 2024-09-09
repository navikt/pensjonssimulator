package no.nav.pensjon.simulator.core.anonym

data class AnonymSimuleringResult(
    val alderspensjonKapittel19Andel: Double,
    val alderspensjonKapittel20Andel: Double,
    val alderspensjonPeriodeListe: List<AnonymPensjonsperiode>,
    val privatAfpPeriodeListe: List<AnonymSimulertPrivatAfpPeriode>,
    val offentligAfpPeriodeListe: List<AnonymSimulertOffentligAfpPeriode>,
)

data class AnonymPensjonsperiode(
    val beloep: Int?,
    val alderAar: Int?,
    val simulertBeregningInformasjon: AnonymSimulertBeregningInformasjon?
)

data class AnonymSimulertPrivatAfpPeriode(
    val alderAar: Int?,
    val aarligBeloep: Int?,
    val maanedligBeloep: Int?
)

data class AnonymSimulertOffentligAfpPeriode(
    val alderAar: Int?,
    val aarligBeloep: Int?,
    val maanedligBeloep: Int?
)

data class AnonymSimulertBeregningInformasjon(
    val spt: Double?,
    val gp: Int?,
    val tp: Int?,
    val ttAnvKap19: Int?,
    val ttAnvKap20: Int?,
    val paE91: Int?,
    val paF92: Int?,
    val forholdstall: Double?,
    val delingstall: Double?,
    val pensjonBeholdningEtterUttak: Int?,
    val inntektPensjon: Int?,
    val garantipensjon: Int?,
)
