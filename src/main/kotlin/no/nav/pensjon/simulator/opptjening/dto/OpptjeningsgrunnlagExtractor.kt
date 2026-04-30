package no.nav.pensjon.simulator.opptjening.dto

import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import java.time.LocalDate

object OpptjeningsgrunnlagExtractor {

    fun fromDto(source: OpptjeningsgrunnlagResponseDto): LoependeInntekt? =
        source.opptjeningsGrunnlag.inntektListe
            .filter(::isSumPensjonsgivendeInntekt)
            .maxByOrNull { it.inntektAr }
            ?.let { LoependeInntekt(aarligBeloep = it.belop, fom = LocalDate.of(it.inntektAr, 1, 1)) }

    private fun isSumPensjonsgivendeInntekt(inntekt: InntektDto): Boolean =
        inntekt.inntektType == "SUM_PI"
}
