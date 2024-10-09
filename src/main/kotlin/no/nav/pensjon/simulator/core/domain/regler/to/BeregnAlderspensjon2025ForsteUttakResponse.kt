package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025

class BeregnAlderspensjon2025ForsteUttakResponse : ServiceResponse() {

    var beregningsResultat: BeregningsResultatAlderspensjon2025? = null
    var beregningsResultatTilRevurdering: BeregningsResultatAlderspensjon2025? = null
}
