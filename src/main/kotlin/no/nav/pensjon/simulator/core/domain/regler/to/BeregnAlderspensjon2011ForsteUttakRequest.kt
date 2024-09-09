package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.Date
import java.util.Vector

class BeregnAlderspensjon2011ForsteUttakRequest(
    var kravhode: Kravhode? = null,
    var vilkarsvedtakListe: MutableList<VilkarsVedtak> = Vector(),
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    var virkFom: Date? = null,
    var virkTom: Date? = null,
    var forholdstallUtvalg: ForholdstallUtvalg? = null,
    var ektefellenMottarPensjon: Boolean = false,
    var afpLivsvarig: AfpLivsvarig? = null
) : ServiceRequest() {
    override fun virkFom(): Date? {
        return this.virkFom
    }

    override fun persons(): String {
        return "" //fetchPersongrunnlagInUseFromKravhode(kravhode)
    }
}
