package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

class RegulerBeregningGrunnlag {

    var beregning1967: Beregning? = null
    var virkFom: Date? = null
    var uttaksgradListe: List<Uttaksgrad> = mutableListOf()
    var brukersVilkarsvedtakListe: List<VilkarsVedtak> = mutableListOf()
    var sokersPersongrunnlag: Persongrunnlag? = null
    var epsPersongrunnlag: Persongrunnlag? = null
    var pakkseddel: Pakkseddel? = null
}
