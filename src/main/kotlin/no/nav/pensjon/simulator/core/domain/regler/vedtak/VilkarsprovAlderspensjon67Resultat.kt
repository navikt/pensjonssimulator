package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning

// Copied from pensjon-regler-api v2.0.0 2026-06-04
class VilkarsprovAlderspensjon67Resultat : AbstraktVilkarsprovResultat() {
    var beregningVedUttak: Beregning? = null
    var halvMinstePensjon = 0
}