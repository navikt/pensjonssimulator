package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025

// 2026-05-05
class RevurderingAlderspensjon2025Response : ServiceResponse() {

    var revurdertBeregningsResultat: BeregningsResultatAlderspensjon2025? = null
    var beregningsresultatTilRevurdering: BeregningsResultatAlderspensjon2025? = null
}
