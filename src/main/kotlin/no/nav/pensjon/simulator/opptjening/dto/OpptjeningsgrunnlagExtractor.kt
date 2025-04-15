package no.nav.pensjon.simulator.opptjening.dto

import no.nav.pensjon.simulator.inntekt.Inntekt
import java.time.LocalDate

object OpptjeningsgrunnlagExtractor {

    fun fromDto(source: OpptjeningsgrunnlagResponseDto) : Inntekt? {
        return source.opptjeningsGrunnlag.inntektListe.filter {
            it.inntektType == "SUM_PI"
        }
            .maxByOrNull { it.inntektAr }
            ?.let { Inntekt(aarligBeloep = it.belop, fom = LocalDate.of(it.inntektAr, 1, 1)) }
    }

    fun zeroInntekt() = Inntekt(0, LocalDate.of(LocalDate.now().year, 1, 1))
}