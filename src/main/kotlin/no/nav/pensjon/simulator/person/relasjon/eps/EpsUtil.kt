package no.nav.pensjon.simulator.person.relasjon.eps

import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

/**
 * EPS = ektefelle/partner/samboer
 */
object EpsUtil {

    // PEN: EktefelleMottarPensjonDecider.isEktefelleMottarPensjon
    fun epsMottarPensjon(personListe: List<Persongrunnlag>): Boolean =
        eps(personListe).inntektsgrunnlagListe.any(::pensjonsinntektFraFolketrygden)

    private fun pensjonsinntektFraFolketrygden(inntekt: Inntektsgrunnlag): Boolean =
        inntekt.inntektTypeEnum == InntekttypeEnum.PENF
                && inntekt.belop > 0

    private fun eps(personListe: List<Persongrunnlag>): Persongrunnlag =
        personListe.firstOrNull(::anyEps) ?: Persongrunnlag()

    private fun anyEps(person: Persongrunnlag): Boolean =
        person.personDetaljListe.any { it.isEps() }
}
