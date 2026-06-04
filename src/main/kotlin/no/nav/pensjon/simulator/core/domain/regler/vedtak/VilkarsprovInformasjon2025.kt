package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger

// 2026-06-04
class VilkarsprovInformasjon2025 : VilkarsprovInformasjon() {
    var garPNvedNormertPensjonsalder: JustertGarantipensjonsniva? = null
    var garPNvedNormertPensjonsalderProRata: JustertGarantipensjonsniva? = null
    var beholdningerVedNormertPensjonsalder: Beholdninger? = null
}
