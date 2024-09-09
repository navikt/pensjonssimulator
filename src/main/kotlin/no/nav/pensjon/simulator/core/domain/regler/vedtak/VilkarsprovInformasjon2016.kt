package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetAfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetPensjonUnderUtbetaling
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling

class VilkarsprovInformasjon2016 : VilkarsprovInformasjon {
    var vektetPensjonsniva = 0.0
    var vektetPensjonsnivaProRata = 0.0
    var vilkarsprovInformasjon2011: VilkarsprovInformasjon2011? = null
    var vilkarsprovInformasjon2025: VilkarsprovInformasjon2025? = null

    constructor() : super()

    constructor(vilkarsprovInformasjon2016: VilkarsprovInformasjon2016) : super(vilkarsprovInformasjon2016) {
        this.vektetPensjonsniva = vilkarsprovInformasjon2016.vektetPensjonsniva
        this.vektetPensjonsnivaProRata = vilkarsprovInformasjon2016.vektetPensjonsnivaProRata
        if (vilkarsprovInformasjon2016.vilkarsprovInformasjon2011 != null) {
            this.vilkarsprovInformasjon2011 =
                VilkarsprovInformasjon2011(vilkarsprovInformasjon2016.vilkarsprovInformasjon2011!!)
        }
        if (vilkarsprovInformasjon2016.vilkarsprovInformasjon2025 != null) {
            this.vilkarsprovInformasjon2025 =
                VilkarsprovInformasjon2025(vilkarsprovInformasjon2016.vilkarsprovInformasjon2025!!)
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
        vektetPensjonsniva: Double = 0.0,
        vektetPensjonsnivaProRata: Double = 0.0,
        vilkarsprovInformasjon2011: VilkarsprovInformasjon2011? = null,
        vilkarsprovInformasjon2025: VilkarsprovInformasjon2025? = null
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
        this.vektetPensjonsniva = vektetPensjonsniva
        this.vektetPensjonsnivaProRata = vektetPensjonsnivaProRata
        this.vilkarsprovInformasjon2011 = vilkarsprovInformasjon2011
        this.vilkarsprovInformasjon2025 = vilkarsprovInformasjon2025
    }
}
