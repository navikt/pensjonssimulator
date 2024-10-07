package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016

class BeregnAlderspensjon2016ForsteUttakResponse : ServiceResponse() {

    var beregningsResultat: BeregningsResultatAlderspensjon2016? = null
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2016? = null
}
