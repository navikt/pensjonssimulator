package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetAfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.FremskrevetPensjonUnderUtbetaling
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.PensjonUnderUtbetaling

// Copied from pensjon-regler-api v2.0.0 2026-06-04
@JsonSubTypes(
    JsonSubTypes.Type(value = VilkarsprovInformasjon2011::class),
    JsonSubTypes.Type(value = VilkarsprovInformasjon2016::class),
    JsonSubTypes.Type(value = VilkarsprovInformasjon2025::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class VilkarsprovInformasjon {
    var ektefelleInntektOver2g = false
    var flyktning = false
    var fullPensjonVedNormertPensjonsalder: FremskrevetPensjonUnderUtbetaling? = null
    var pensjonVedUttak: PensjonUnderUtbetaling? = null
    var fremskrevetAfpLivsvarig: FremskrevetAfpLivsvarig? = null
    var afpPrivatLivsvarigVedUttak: AfpPrivatLivsvarig? = null
    var afpLivsvarigBrukt = false
    var fremskrevetPensjonVedNormertPensjonsAlder = 0.0
    var samletPensjonVedNormertPensjonsAlder = 0.0
}