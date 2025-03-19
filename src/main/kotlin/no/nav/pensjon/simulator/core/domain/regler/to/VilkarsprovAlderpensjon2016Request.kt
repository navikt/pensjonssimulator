package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2016
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.util.*

// 2025-03-18
class VilkarsprovAlderpensjon2016Request : ServiceRequest() {
    var kravhode: Kravhode? = null
    var virkFom: Date? = null
    @Deprecated("Avvikles. Erstattes av afpPrivatLivsvarig.")
    var afpLivsvarig: AfpLivsvarig? = null
    var afpPrivatLivsvarig: AfpPrivatLivsvarig? = null
    var afpVirkFom: Date? = null
    var sisteBeregning: SisteAldersberegning2016? = null
    var utforVilkarsberegning = false
}
