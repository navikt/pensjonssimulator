package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

object FppSimuleringUtil {

    //TODO merge with findPersongrunnlagHavingRolle
    fun persongrunnlagForRolle(
        grunnlagListe: List<Persongrunnlag>,
        rolle: GrunnlagsrolleEnum
    ): Persongrunnlag? =
        grunnlagListe.firstOrNull { p ->
            p.personDetaljListe.any { rolle == it.grunnlagsrolleEnum }
        }
}
