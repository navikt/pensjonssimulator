package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class VilkarsprovRequest : ServiceRequest {

    var kravhode: Kravhode? = null
    var sisteBeregning: SisteBeregning? = null
    var fom: Date? = null
    var tom: Date? = null
    var vilkarsvedtakliste: List<VilkarsVedtak> = mutableListOf()

    constructor()

    constructor(kravhode: Kravhode?, sisteBeregning: SisteBeregning?, fom: Date?, tom: Date?) {
        this.kravhode = kravhode
        this.sisteBeregning = sisteBeregning
        this.fom = fom
        this.tom = tom
        this.vilkarsvedtakliste = mutableListOf()
    }
}
