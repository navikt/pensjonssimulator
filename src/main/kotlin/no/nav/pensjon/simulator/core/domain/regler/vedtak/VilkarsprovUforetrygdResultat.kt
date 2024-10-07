package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsresultatUforetrygd

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
