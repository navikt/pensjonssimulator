package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016

class RevurderingAlderspensjon2016Response : ServiceResponse() {

    var revurdertBeregningsResultat: BeregningsResultatAlderspensjon2016? = null
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2016? = null
}
