package no.nav.pensjon.simulator.opptjening.dto

data class OpptjeningsgrunnlagResponseDto(val opptjeningsGrunnlag: OpptjeningsgrunnlagDto)
data class OpptjeningsgrunnlagDto(var inntektListe: List<InntektDto>)
data class InntektDto(val inntektType: String, val inntektAr: Int, val belop: Int)

