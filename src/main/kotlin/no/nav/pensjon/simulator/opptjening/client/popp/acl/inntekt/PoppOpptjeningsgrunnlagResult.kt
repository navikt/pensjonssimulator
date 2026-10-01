package no.nav.pensjon.simulator.opptjening.client.popp.acl.inntekt

import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningPOPPTypeEnum
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import java.time.LocalDate

data class PoppOpptjeningsgrunnlagResult(
    val opptjeningsGrunnlag: PoppOpptjeningsgrunnlag
) {
    fun toInternalValue(): LoependeInntekt? =
        opptjeningsGrunnlag.toInternalValue()
}

data class PoppOpptjeningsgrunnlag(
    val inntektListe: List<PoppInntekt>
) {
    fun toInternalValue(): LoependeInntekt? =
        inntektListe
            .filter { it.isSumPensjonsgivendeInntekt }
            .maxByOrNull { it.inntektAr }
            ?.loependeInntekt()
}

data class PoppInntekt(
    val inntektType: String,
    val inntektAr: Int,
    val belop: Int
) {
    val isSumPensjonsgivendeInntekt: Boolean = inntektType == OpptjeningPOPPTypeEnum.SUM_PI.name

    fun loependeInntekt() =
        LoependeInntekt(
            aarligBeloep = belop,
            fom = LocalDate.of(inntektAr, 1, 1)
        )
}