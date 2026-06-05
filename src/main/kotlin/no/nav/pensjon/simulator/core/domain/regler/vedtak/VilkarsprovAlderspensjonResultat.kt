package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat

// Copied from pensjon-regler-api v2.0.0 2026-06-04
class VilkarsprovAlderspensjonResultat : AbstraktVilkarsprovResultat() {
    var beregningVedUttak: AbstraktBeregningsResultat? = null
    var vilkarsprovInformasjon: VilkarsprovInformasjon? = null
}
