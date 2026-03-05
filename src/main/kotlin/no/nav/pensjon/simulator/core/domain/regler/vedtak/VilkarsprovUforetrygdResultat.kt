package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsresultatUforetrygd

// Copied from pensjon-regler-api 2026-03-05
class VilkarsprovUforetrygdResultat : AbstraktVilkarsprovResultat() {
    /**
     * Beregning av uføretrygden ved vilkårsprøving av halv minsteytelse.
     */
    var beregningsresultatUforetrygd: BeregningsresultatUforetrygd? = null

    /**
     * Beregnet halv minsteytelse;
     */
    var halvMinsteytelse = 0
}
