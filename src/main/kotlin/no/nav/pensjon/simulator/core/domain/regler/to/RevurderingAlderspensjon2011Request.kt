package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class RevurderingAlderspensjon2011Request(
    var kravhode: Kravhode? = null,
    var vilkarsvedtakListe: Vector<VilkarsVedtak> = Vector(),
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null,
    var epsMottarPensjon: Boolean = false,
    var forholdstallUtvalg: ForholdstallUtvalg? = null,
    var virkFom: Date? = null,
    var virkTom: Date? = null,
    var forrigeAldersBeregning: SisteAldersberegning2011? = null,
    var afpLivsvarig: AfpLivsvarig? = null
) : ServiceRequest() {
    override fun virkFom(): Date? {
        return this.virkFom
    }

    override fun persons(): String {
        return "" //fetchPersongrunnlagInUseFromKravhode(kravhode)
    }
}
