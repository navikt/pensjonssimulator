package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag

data class PoppPensjonspoeng(
    val pensjonspoengType: String? = null, // OpptjeningtypeEnum
    val inntekt: PoppInntekt? = null,
    val ar: Int? = null,
    val anvendtPi: Int? = null,
    val poeng: Double? = null,
    val maxUforegrad: Int? = null
) {
    fun toGrunnlag() =
        Opptjeningsgrunnlag().also {
            it.ar = ar ?: 0
            it.pp = poeng ?: 0.0
            it.opptjeningTypeEnum = opptjeningType()

            if (it.opptjeningTypeEnum == OpptjeningtypeEnum.PPI) {
                it.pi = inntekt?.belop?.toInt() ?: 0
                it.pia = anvendtPi ?: 0
                it.maksUforegrad = maxUforegrad ?: 0
            }
        }

    private fun opptjeningType(): OpptjeningtypeEnum? =
        pensjonspoengType?.let { enumValueOf<OpptjeningtypeEnum>(it) }
}