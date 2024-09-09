package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import java.util.Date

class RevurderingAlderspensjon2016Response(
    var revurdertBeregningsResultat: BeregningsResultatAlderspensjon2016? = null,
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2016? = null,
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? {
        return revurdertBeregningsResultat?.virkFom
    }

    override fun persons(): String {
        return "" //fetchPersonIdFromAbstraktBeregningsResultat(revurdertBeregningsResultat)
    }
}
