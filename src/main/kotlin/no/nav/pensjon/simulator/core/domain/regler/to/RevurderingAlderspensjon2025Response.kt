package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import java.util.*

class RevurderingAlderspensjon2025Response(
    var revurdertBeregningsResultat: BeregningsResultatAlderspensjon2025? = null,
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2025? = null,
    override val pakkseddel: Pakkseddel = Pakkseddel()
) : ServiceResponse(pakkseddel) {
    override fun virkFom(): Date? = revurdertBeregningsResultat?.virkFom

    override fun persons(): String = ""
}
