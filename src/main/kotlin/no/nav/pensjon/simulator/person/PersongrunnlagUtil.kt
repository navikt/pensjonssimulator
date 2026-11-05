package no.nav.pensjon.simulator.person

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

object PersongrunnlagUtil {

    fun persongrunnlagForRolle(
        grunnlagListe: List<Persongrunnlag>,
        rolle: GrunnlagsrolleEnum
    ): Persongrunnlag? =
        grunnlagListe.firstOrNull { it.harRolle(rolle) }
}