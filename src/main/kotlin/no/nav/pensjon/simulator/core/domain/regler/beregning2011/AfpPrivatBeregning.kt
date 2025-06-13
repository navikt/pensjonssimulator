package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening

// 2025-06-13
class AfpPrivatBeregning : Beregning2011() {
    var afpPrivatLivsvarig: AfpPrivatLivsvarig? = null
    var afpKompensasjonstillegg: AfpKompensasjonstillegg? = null
    var afpKronetillegg: AfpKronetillegg? = null
    var afpOpptjening: AfpOpptjening? = null
}
