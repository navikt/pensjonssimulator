package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger

class VilkarsprovInformasjon2025 : VilkarsprovInformasjon() {

    var garPN67: JustertGarantipensjonsniva? = null
    var garPN67ProRata: JustertGarantipensjonsniva? = null
    var beholdningerVed67: Beholdninger? = null
}
