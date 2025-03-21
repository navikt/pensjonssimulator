package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetAfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetPensjonUnderUtbetaling
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling

// 2025-03-18
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
    //@Deprecated("Avvikles. Erstattes av afpPrivatLivsvarigVedUttak.")
    //var afpLivsvarigVedUttak: AfpLivsvarig? = null
    var afpPrivatLivsvarigVedUttak: AfpPrivatLivsvarig? = null
    var afpLivsvarigBrukt = false
    var fremskrevetPensjonVed67 = 0.0
    var samletPensjonVed67PerAr = 0.0
}
