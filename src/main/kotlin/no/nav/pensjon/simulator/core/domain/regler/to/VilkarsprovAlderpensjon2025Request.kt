package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOffentligLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.util.*

class VilkarsprovAlderpensjon2025Request(
    var kravhode: Kravhode? = null,
    var fom: Date? = null,
    var forholdstallUtvalg: ForholdstallUtvalg? = null,
    var delingstallUtvalg: DelingstallUtvalg? = null,
    var afpLivsvarig: AfpLivsvarig? = null,
    var afpVirkFom: Date? = null,
    var sisteBeregning: SisteAldersberegning2011? = null,
    var utforVilkarsberegning: Boolean = false,
    var garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag? = null,
    var afpOffentligLivsvarigGrunnlag: AfpOffentligLivsvarig? = null,
) : ServiceRequest() {
    override fun virkFom(): Date? = this.fom

    override fun persons(): String = ""
}
