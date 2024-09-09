package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetAfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetPensjonUnderUtbetaling
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling

@JsonSubTypes(
    JsonSubTypes.Type(value = VilkarsprovInformasjon2011::class),
    JsonSubTypes.Type(value = VilkarsprovInformasjon2016::class),
    JsonSubTypes.Type(value = VilkarsprovInformasjon2025::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class VilkarsprovInformasjon {
    var ektefelleInntektOver2g = false
    var flyktning = false
    var fullPensjonVed67: FremskrevetPensjonUnderUtbetaling? = null
    var pensjonVedUttak: PensjonUnderUtbetaling? = null
    var fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig? = null
    var afpLivsvarigVedUttak: AfpLivsvarig? = null
    var afpLivsvarigBrukt = false
    var fremskrevetPensjonVed67 = 0.0
    var samletPensjonVed67PerAr = 0.0

    constructor()

    constructor(vi: VilkarsprovInformasjon) : this() {
        this.ektefelleInntektOver2g = vi.ektefelleInntektOver2g
        this.flyktning = vi.flyktning
        if (vi.fullPensjonVed67 != null) {
            this.fullPensjonVed67 = FremskrevetPensjonUnderUtbetaling(vi.fullPensjonVed67!!)
        }
        if (vi.pensjonVedUttak != null) {
            this.pensjonVedUttak = PensjonUnderUtbetaling(vi.pensjonVedUttak!!)
        }
        if (vi.fremskrevetAfpLivsvarig != null) {
            this.fremskrevetAfpLivsvarig = FremskrevetAfpLivsvarig(vi.fremskrevetAfpLivsvarig!!)
        }
        if (vi.afpLivsvarigVedUttak != null) {
            this.afpLivsvarigVedUttak = AfpLivsvarig(vi.afpLivsvarigVedUttak!!)
        }
        this.afpLivsvarigBrukt = vi.afpLivsvarigBrukt
        this.fremskrevetPensjonVed67 = vi.fremskrevetPensjonVed67
        this.samletPensjonVed67PerAr = vi.samletPensjonVed67PerAr
    }

    constructor(
        ektefelleInntektOver2g: Boolean = false,
        flyktning: Boolean = false,
        fullPensjonVed67: FremskrevetPensjonUnderUtbetaling? = null,
        pensjonVedUttak: PensjonUnderUtbetaling? = null,
        fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig? = null,
        afpLivsvarigVedUttak: AfpLivsvarig? = null, afpLivsvarigBrukt: Boolean = false,
        fremskrevetPensjonVed67: Double = 0.0,
        samletPensjonVed67PerAr: Double = 0.0
    ) {
        this.ektefelleInntektOver2g = ektefelleInntektOver2g
        this.flyktning = flyktning
        this.fullPensjonVed67 = fullPensjonVed67
        this.pensjonVedUttak = pensjonVedUttak
        this.fremskrevetAfpLivsvarig = fremskrevetAfpLivsvarig
        this.afpLivsvarigVedUttak = afpLivsvarigVedUttak
        this.afpLivsvarigBrukt = afpLivsvarigBrukt
        this.fremskrevetPensjonVed67 = fremskrevetPensjonVed67
        this.samletPensjonVed67PerAr = samletPensjonVed67PerAr
    }
}
