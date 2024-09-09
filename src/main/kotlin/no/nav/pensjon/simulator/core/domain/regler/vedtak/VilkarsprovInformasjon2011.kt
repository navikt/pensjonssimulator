package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*

class VilkarsprovInformasjon2011 : VilkarsprovInformasjon {
    var mpn67: JustertMinstePensjonsniva? = null
    var mpn67ProRata: JustertMinstePensjonsniva? = null

    constructor(vilkarsprovInformasjon2011: VilkarsprovInformasjon2011) : super(vilkarsprovInformasjon2011) {
        if (vilkarsprovInformasjon2011.mpn67 != null) {
            this.mpn67 = JustertMinstePensjonsniva(vilkarsprovInformasjon2011.mpn67!!)
        }
        if (vilkarsprovInformasjon2011.mpn67ProRata != null) {
            this.mpn67ProRata = JustertMinstePensjonsniva(vilkarsprovInformasjon2011.mpn67ProRata!!)
        }
    }

    constructor() : super()

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
        mpn67: JustertMinstePensjonsniva? = null,
        mpn67ProRata: JustertMinstePensjonsniva? = null
    )
            : super(
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
        this.mpn67 = mpn67
        this.mpn67ProRata = mpn67ProRata
    }
}
