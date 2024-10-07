package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011

class RevurderingAlderspensjon2011Response : ServiceResponse() {

    var revurdertBeregningsResultat: BeregningsResultatAlderspensjon2011? = null
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2011? = null
}
