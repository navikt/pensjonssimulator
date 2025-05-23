package no.nav.pensjon.simulator.opptjening.dto

import no.nav.pensjon.simulator.inntekt.Inntekt
import java.time.LocalDate

object OpptjeningsgrunnlagExtractor {

    fun fromDto(source: OpptjeningsgrunnlagResponseDto): Inntekt? =
        source.opptjeningsGrunnlag.inntektListe
            .filter(::isSumPensjonsgivendeInntekt)
            .maxByOrNull { it.inntektAr }
            ?.let { Inntekt(aarligBeloep = it.belop, fom = LocalDate.of(it.inntektAr, 1, 1)) }

    private fun isSumPensjonsgivendeInntekt(inntekt: InntektDto): Boolean =
        inntekt.inntektType == "SUM_PI"
}
