package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.util.*

// 2025-03-18
class VilkarsprovAlderpensjon2011Request : ServiceRequest() {
    var kravhode: Kravhode? = null
    var fom: Date? = null
    var tom: Date? = null
    var afpVirkFom: Date? = null
    @Deprecated("Avvikles. Erstattes av afpPrivatLivsvarig.")
    var afpLivsvarig: AfpLivsvarig? = null
    var afpPrivatLivsvarig: AfpPrivatLivsvarig? = null
    var sisteBeregning: SisteAldersberegning2011? = null
    var utforVilkarsberegning = false
}
