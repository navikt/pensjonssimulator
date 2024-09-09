package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.util.*

class BeregningsresultatUforetrygd : AbstraktBeregningsResultat {

    var uforetrygdberegning: Uforetrygdberegning? = null

    constructor() : super()

    constructor(ur: BeregningsresultatUforetrygd) : super(ur) {
        if (ur.uforetrygdberegning != null) {
            uforetrygdberegning = Uforetrygdberegning(ur.uforetrygdberegning!!, true)
        }
    }

    constructor(
        uforetrygdberegning: Uforetrygdberegning? = null,
        /** super AbstraktBeregningsResultat */
            virkFom: Date? = null,
        pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
        uttaksgrad: Int = 0,
        brukersSivilstand: SivilstandTypeCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        beregningArsak: BeregningArsakCti? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        gjennomsnittligUttaksgradSisteAr: Double = 0.0
    ) : super(
            virkFom = virkFom,
            pensjonUnderUtbetaling = pensjonUnderUtbetaling,
            uttaksgrad = uttaksgrad,
            brukersSivilstand = brukersSivilstand,
            benyttetSivilstand = benyttetSivilstand,
            beregningArsak = beregningArsak,
            lonnsvekstInformasjon = lonnsvekstInformasjon,
            merknadListe = merknadListe,
            gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
    ) {
        this.uforetrygdberegning = uforetrygdberegning
    }
}
