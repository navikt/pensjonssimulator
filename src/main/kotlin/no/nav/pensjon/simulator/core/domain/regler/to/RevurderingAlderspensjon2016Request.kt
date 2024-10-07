package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2016
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class RevurderingAlderspensjon2016Request : ServiceRequest() {
    var kravhode: Kravhode? = null
    var vilkarsvedtakListe: List<VilkarsVedtak> = Vector() //TODO mutableListOf()?
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null
    var epsMottarPensjon = false
    var forholdstallUtvalg: ForholdstallUtvalg? = null
    var delingstallUtvalg: DelingstallUtvalg? = null
    var virkFom: Date? = null
    var forrigeAldersBeregning: SisteAldersberegning2016? = null
    var afpLivsvarig: AfpLivsvarig? = null

    /**
     * Grunnlag for beregning av garantitilleggsbeholdning.
     * (Ref. BER3156)
     */
    var garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag? = null
}
