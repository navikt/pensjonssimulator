package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate
import java.util.Vector

// 2026-06-04
class BeregnAlderspensjon2025ForsteUttakRequest : ServiceRequest() {
    var virkFomLd: LocalDate? = null
    var kravhode: Kravhode? = null
    var vilkarsvedtakListe: List<VilkarsVedtak> = Vector()
    var epsMottarPensjon = false
    var afpPrivatLivsvarig: AfpPrivatLivsvarig? = null
    var afpOffentligLivsvarigGrunnlag: AfpOffentligLivsvarigGrunnlag? = null
}