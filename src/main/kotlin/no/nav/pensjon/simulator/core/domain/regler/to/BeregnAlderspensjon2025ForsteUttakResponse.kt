package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import java.util.*

class BeregnAlderspensjon2025ForsteUttakResponse(
    var beregningsResultat: BeregningsResultatAlderspensjon2025? = null,
    var beregningsResultatTilRevurdering: BeregningsResultatAlderspensjon2025? = null
) : ServiceResponse() {
    override fun virkFom(): Date? = beregningsResultat?.virkFom

    override fun persons(): String = ""
}
