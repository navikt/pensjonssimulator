package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InfoPavirkendeYtelse
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class RevurderingAlderspensjon2025Request : ServiceRequest() {
    var kravhode: Kravhode? = null
    var vilkarsvedtakListe: ArrayList<VilkarsVedtak> = ArrayList()
    var infoPavirkendeYtelse: InfoPavirkendeYtelse? = null
    var epsMottarPensjon = false
    var forholdstallUtvalg: ForholdstallUtvalg? = null
    var delingstallUtvalg: DelingstallUtvalg? = null
    var virkFom: Date? = null
    var sisteAldersBeregning2011: SisteAldersberegning2011? = null
    var afpLivsvarig: AfpLivsvarig? = null
    var garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag? = null
    var afpOffentligLivsvarigGrunnlag: AfpOffentligLivsvarigGrunnlag? = null
}
