package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening

class AfpPrivatBeregning : Beregning2011() {

    var afpLivsvarig: AfpLivsvarig? = null
    var afpKompensasjonstillegg: AfpKompensasjonstillegg? = null
    var afpKronetillegg: AfpKronetillegg? = null
    var afpOpptjening: AfpOpptjening? = null
}
