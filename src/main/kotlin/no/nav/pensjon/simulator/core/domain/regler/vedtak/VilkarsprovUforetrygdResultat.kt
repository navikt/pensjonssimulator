package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsresultatUforetrygd

class VilkarsprovUforetrygdResultat : AbstraktVilkarsprovResultat {

    /**
     * Beregning av uføretrygden ved vilkårsprøving av halv minsteytelse.
     */
    var beregningsresultatUforetrygd: BeregningsresultatUforetrygd? = null

    /**
     * Beregnet halv minsteytelse;
     */
    var halvMinsteytelse: Int = 0

    constructor() : super()

    constructor(resultat: VilkarsprovUforetrygdResultat) : super() {
        this.beregningsresultatUforetrygd = BeregningsresultatUforetrygd(resultat.beregningsresultatUforetrygd!!)
        this.halvMinsteytelse = resultat.halvMinsteytelse
    }

    constructor(
        beregningsresultatUforetrygd: BeregningsresultatUforetrygd? = null,
        halvMinsteytelse: Int = 0
    ) : super() {
        this.beregningsresultatUforetrygd = beregningsresultatUforetrygd
        this.halvMinsteytelse = halvMinsteytelse
    }
}
