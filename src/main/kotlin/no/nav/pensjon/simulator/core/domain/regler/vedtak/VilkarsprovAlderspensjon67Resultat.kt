package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning

class VilkarsprovAlderspensjon67Resultat : AbstraktVilkarsprovResultat() {

    var beregningVedUttak: Beregning? = null
    var halvMinstePensjon = 0
}
