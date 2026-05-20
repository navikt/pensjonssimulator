package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2016
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate

// 2026-05-05
class VilkarsprovAlderpensjon2016Request : ServiceRequest() {
    var kravhode: Kravhode? = null
    var virkFomLd: LocalDate? = null
    var afpPrivatLivsvarig: AfpPrivatLivsvarig? = null
    var afpVirkFomLd: LocalDate? = null
    var sisteBeregning: SisteAldersberegning2016? = null
    var utforVilkarsberegning = false
}