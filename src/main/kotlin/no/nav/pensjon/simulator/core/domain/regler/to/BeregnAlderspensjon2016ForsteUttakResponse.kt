package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import java.util.Date

class BeregnAlderspensjon2016ForsteUttakResponse(
    var beregningsResultat: BeregningsResultatAlderspensjon2016? = null,
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2016? = null,
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? {
        return beregningsResultat?.virkFom
    }

    override fun persons(): String {
        return "" //TransferObjectUtil.fetchPersonIdFromAbstraktBeregningsResultat(beregningsResultat)
    }
}
