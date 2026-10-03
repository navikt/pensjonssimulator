package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSamling
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.EgressException

data class PoppOpptjeningsgrunnlag(
    val fnr: String? = null,
    val inntektListe: List<PoppInntekt>? = emptyList(),
    val omsorgListe: List<PoppOmsorg>? = emptyList(),
    val dagpengerListe: List<PoppDagpenger>? = emptyList(),
    val forstegangstjeneste: PoppFoerstegangstjeneste? = null
) {
    fun toInternalValue() =
        OpptjeningsgrunnlagSamling(
            pid = fnr?.let(::Pid) ?: throw EgressException("udefinert fnr fra POPP"),
            inntektListe = inntektListe.orEmpty().map { it.toGrunnlag() },
            omsorgListe = omsorgListe.orEmpty().map { it.toGrunnlag() },
            dagpengeListe = dagpengerListe.orEmpty().map { it.toGrunnlag() },
            foerstegangstjeneste = forstegangstjeneste?.toGrunnlag()
        )

    fun toLoependeInntekt(): LoependeInntekt? =
        inntektListe.orEmpty()
            .filter { it.isSumPensjonsgivendeInntekt }
            .maxByOrNull { it.inntektAr ?: 0 }
            ?.toLoependeInntekt()
}