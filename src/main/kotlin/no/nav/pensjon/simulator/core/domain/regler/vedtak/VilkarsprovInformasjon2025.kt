package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger

class VilkarsprovInformasjon2025 : VilkarsprovInformasjon {
    var garPN67: JustertGarantipensjonsniva? = null
    var garPN67ProRata: JustertGarantipensjonsniva? = null
    var beholdningerVed67: Beholdninger? = null

    constructor() : super()

    constructor(vilkarsprovInformasjon2025: VilkarsprovInformasjon2025) : super(vilkarsprovInformasjon2025) {
        if (vilkarsprovInformasjon2025.garPN67 != null) {
            this.garPN67 = JustertGarantipensjonsniva(vilkarsprovInformasjon2025.garPN67!!)
        }
        if (vilkarsprovInformasjon2025.garPN67ProRata != null) {
            this.garPN67ProRata = JustertGarantipensjonsniva(vilkarsprovInformasjon2025.garPN67ProRata!!)
        }
        if (vilkarsprovInformasjon2025.beholdningerVed67 != null) {
            this.beholdningerVed67 = Beholdninger(vilkarsprovInformasjon2025.beholdningerVed67!!)
        }
    }

    constructor(
        ektefelleInntektOver2g: Boolean = false,
        flyktning: Boolean = false,
        fullPensjonVed67: FremskrevetPensjonUnderUtbetaling? = null,
        pensjonVedUttak: PensjonUnderUtbetaling? = null,
        fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig? = null,
        afpLivsvarigVedUttak: AfpLivsvarig? = null,
        afpLivsvarigBrukt: Boolean = false,
        fremskrevetPensjonVed67: Double = 0.0,
        samletPensjonVed67PerAr: Double = 0.0,
        /** Interne felt */
        garPN67: JustertGarantipensjonsniva? = null,
        garPN67ProRata: JustertGarantipensjonsniva? = null,
        beholdningerVed67: Beholdninger? = null
    ) : super(
        ektefelleInntektOver2g,
        flyktning,
        fullPensjonVed67,
        pensjonVedUttak,
        fremskrevetAfpLivsvarig,
        afpLivsvarigVedUttak,
        afpLivsvarigBrukt,
        fremskrevetPensjonVed67,
        samletPensjonVed67PerAr
    ) {
        this.garPN67 = garPN67
        this.garPN67ProRata = garPN67ProRata
        this.beholdningerVed67 = beholdningerVed67
    }
}
