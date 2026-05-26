package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

// 2026-05-05
class VilkarsprovRequest : ServiceRequest {

    var kravhode: Kravhode? = null
    var sisteBeregning: SisteBeregning? = null
    var fomLd: LocalDate? = null
    var tomLd: LocalDate? = null
    var vilkarsvedtakliste: List<VilkarsVedtak> = mutableListOf()

    constructor()

    constructor(kravhode: Kravhode?, sisteBeregning: SisteBeregning?, fom: LocalDate?, tom: LocalDate?) {
        this.kravhode = kravhode
        this.sisteBeregning = sisteBeregning
        this.fomLd = fom
        this.tomLd = tom
        this.vilkarsvedtakliste = mutableListOf()
    }
}