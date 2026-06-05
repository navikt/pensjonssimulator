package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import java.util.*

// Copied from pensjon-regler-api v2.0.0 2026-06-04
class BeregnAlderspensjon2011ForsteUttakRequest : ServiceRequest() {
    var kravhode: Kravhode? = null
    var vilkarsvedtakListe: List<VilkarsVedtak> = Vector()
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null
    var virkFomLd: LocalDate? = null
    var virkTomLd: LocalDate? = null
    var ektefellenMottarPensjon = false
    var afpPrivatLivsvarig: AfpPrivatLivsvarig? = null
}