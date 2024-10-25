package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.result

import com.fasterxml.jackson.annotation.JsonInclude

// no.nav.domain.pensjon.kjerne.simulering.forenklet.ForenkletSimuleringResultat
data class AnonymSimuleringResultV1(
    val alderspensjonAndelKapittel19: Double,
    val alderspensjonAndelKapittel20: Double,
    val alderspensjonPerioder: List<AnonymPensjonPeriodeV1>,
    val afpPrivatPerioder: List<AnonymSimulertPrivatAfpPeriodeV1>,
    val afpOffentligPerioder: List<AnonymSimulertOffentligAfpPeriodeV1>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AnonymPensjonPeriodeV1(
    val belop: Int?,
    val alder: Int?,
    val simulertBeregningsinformasjon: AnonymSimulertBeregningInformasjonV1?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AnonymSimulertPrivatAfpPeriodeV1(
    val alder: Int?,
    val belopArlig: Int?,
    val belopMnd: Int?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AnonymSimulertOffentligAfpPeriodeV1(
    val alder: Int?,
    val belopArlig: Int?,
    val belopMnd: Int?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AnonymSimulertBeregningInformasjonV1(
    val spt: Double?,
    val gp: Int?,
    val tp: Int?,
    val ttAnvKap19: Int?,
    val ttAnvKap20: Int?,
    val paE91: Int?,
    val paF92: Int?,
    val forholdstall: Double?,
    val delingstall: Double?,
    val pensjonsbeholdningEtterUttak: Int?,
    val inntektspensjon: Int?,
    val garantipensjon: Int?
)
