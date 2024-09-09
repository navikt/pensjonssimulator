package no.nav.pensjon.simulator.core.afp.privat

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forholdstall

// no.nav.service.pensjon.fpen.HentSatserAFPPrivatResponse
data class PrivatAfpSatser(
    val ft: Forholdstall? = null,
    /* TODO:
    val justeringsbelop: Justeringsbelop? = null,
    val ftKomp: ForholdstallKompensasjonstillegg? = null,
    val referansebelop: Referansebelop? = null
    */
)
