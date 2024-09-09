package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.util.*

class BeregningsResultatAfpPrivat : AbstraktBeregningsResultat {

    override fun hentBeregningsinformasjon(): BeregningsInformasjon? = null // SIMDOM-ADD; no beregningsinformasjon for AFP privat

    var afpPrivatBeregning: AfpPrivatBeregning? = null

    constructor() : super()

    constructor(afpPrivatBeregning: AfpPrivatBeregning) : this() {
        this.afpPrivatBeregning = afpPrivatBeregning
    }

    constructor(res: BeregningsResultatAfpPrivat) : super(res) {
        if (res.afpPrivatBeregning != null) {
            afpPrivatBeregning = AfpPrivatBeregning(res.afpPrivatBeregning!!)
        }
    }

    constructor(
        afpPrivatBeregning: AfpPrivatBeregning? = null,
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
        this.afpPrivatBeregning = afpPrivatBeregning
    }
}
