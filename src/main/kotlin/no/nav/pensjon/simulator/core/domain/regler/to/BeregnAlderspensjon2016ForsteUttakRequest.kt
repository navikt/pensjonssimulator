package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.Date

class BeregnAlderspensjon2016ForsteUttakRequest(
    var kravhode: Kravhode? = null,
    var vilkarsvedtakListe: MutableList<VilkarsVedtak> = mutableListOf(),
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    var virkFom: Date? = null,
    var forholdstallUtvalg: ForholdstallUtvalg? = null,
    var delingstallUtvalg: DelingstallUtvalg? = null,
    var epsMottarPensjon: Boolean = false,
    var afpLivsvarig: AfpLivsvarig? = null,
    var garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag? = null
) : ServiceRequest() {
    override fun virkFom(): Date? {
        return this.virkFom
    }

    override fun persons(): String {
        return "" //fetchPersongrunnlagInUseFromKravhode(kravhode)
    }
}
