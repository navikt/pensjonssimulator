package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import java.util.*

class Uforetidspunkt : AbstraktBeregningsvilkar {
    /**
     * Angir det tidligste året som kan påvirke opptjeningen for dette uføretidspunktet.
     */
    var tidligstVurderteAr = 0
    var uforetidspunkt: Date? = null

    /**
     * Dato for når man var sist innmeldt i folketrygden. Benyttes for fremtidig trygdetid.
     */
    var sistMedlTrygden: Date? = null

    constructor() : super()
    constructor(tidligstVurderteAr: Int, uforetidspunkt: Date?) : super() {
        this.tidligstVurderteAr = tidligstVurderteAr
        this.uforetidspunkt = uforetidspunkt
    }

    constructor(uforetidspunkt: Uforetidspunkt) : super(uforetidspunkt) {
        this.tidligstVurderteAr = uforetidspunkt.tidligstVurderteAr
        this.uforetidspunkt = uforetidspunkt.uforetidspunkt
        this.sistMedlTrygden = uforetidspunkt.sistMedlTrygden
    }

    constructor(
        merknadListe: MutableList<Merknad> = mutableListOf(),
        /** Interne felt */
        tidligstVurderteAr: Int = 0,
        uforetidspunkt: Date? = null,
        sistMedlTrygden: Date? = null
    ) : super(merknadListe) {
        this.tidligstVurderteAr = tidligstVurderteAr
        this.uforetidspunkt = uforetidspunkt
        this.sistMedlTrygden = sistMedlTrygden
    }

    override fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar? {
        var ut: Uforetidspunkt? = null
        if (abstraktBeregningsvilkar.javaClass == Uforetidspunkt::class.java) {
            ut = Uforetidspunkt(abstraktBeregningsvilkar as Uforetidspunkt)
        }
        return ut
    }
}
