package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningPOPPTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import java.time.LocalDate

data class PoppInntekt(
    val inntektAr: Int? = null,
    val belop: Long? = null,
    val inntektType: String? = null, // OpptjeningPOPPTypeEnum
    val kilde: String? = null // GrunnlagkildeEnum
) {
    val isSumPensjonsgivendeInntekt: Boolean = inntektType == OpptjeningPOPPTypeEnum.SUM_PI.name

    fun erRelevant() =
        (inntektAr ?: 0) <= SISTE_SPESIALBEHANDLINGSAAR && typeErRelevant()

    fun toGrunnlag() =
        Inntektsgrunnlag().also {
            it.inntektTypeEnum = inntekttypeEnum()
            it.fomLd = foersteDag(inntektAr ?: 0)
            it.tomLd = sisteDag(inntektAr ?: 0)
            it.belop = belop?.toInt() ?: 0
            // Sett POPP som kilde, ref. PEN GrunnlagForOpptjeningerHelper.addInntektsgrunnlag:
            it.grunnlagKildeEnum = GrunnlagkildeEnum.POPP // NB: ikke fromValue(kilde)
            it.bruk = true
            it.erRelevant = erRelevant()
            it.poppOpptjeningType = inntektType?.let(OpptjeningPOPPTypeEnum::valueOf)
        }

    fun toLoependeInntekt(): LoependeInntekt? =
        inntektAr?.let {
            LoependeInntekt(
                aarligBeloep = belop?.toInt() ?: 0,
                fom = LocalDate.of(it, 1, 1)
            )
        }

    private fun typeErRelevant(): Boolean =
        inntektType?.let(relevanteInntektstyper::contains) == true

    private fun inntekttypeEnum(): InntekttypeEnum? =
        when (inntektType) {
            OpptjeningPOPPTypeEnum.PI66.name -> InntekttypeEnum.ARBLIGN
            OpptjeningPOPPTypeEnum.AI.name -> InntekttypeEnum.AI // Antatt inntekt
            else -> null
        }

    private companion object {
        /**
         * For personer som har uføretidspunkt før 1970 skal inntekter for årene til og med 1967 behandles spesielt,
         * og tjenesten må derfor kopiere over inntektsinformasjon slik beskrevet for å støtte dette.
         */
        private const val SISTE_SPESIALBEHANDLINGSAAR: Int = 1967

        private val relevanteInntektstyper: Set<String> =
            setOf(OpptjeningPOPPTypeEnum.PI66.name, OpptjeningPOPPTypeEnum.AI.name)
    }
}