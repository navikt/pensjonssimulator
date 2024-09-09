package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class VilkarsprovRequest(
    var kravhode: Kravhode? = null,
    var sisteBeregning: SisteBeregning? = null,
    var fom: Date? = null,
    var tom: Date? = null,

    /**
     * @uml.annotations for `vilkarsvedtakliste`
     * collection_type="no.nav.domain.pensjon.kjerne.vedtak.Vilkarsvedtak"
     */
    var vilkarsvedtakliste: MutableList<VilkarsVedtak> = mutableListOf()
) : ServiceRequest() {
    override fun virkFom(): Date? = this.fom

    override fun persons(): String = ""
}
