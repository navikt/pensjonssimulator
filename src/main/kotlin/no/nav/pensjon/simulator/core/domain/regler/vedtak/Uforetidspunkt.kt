package no.nav.pensjon.simulator.core.domain.regler.vedtak

import java.util.Date

// Copied from pensjon-regler-api 2026-01-16
class Uforetidspunkt : AbstraktBeregningsvilkar() {
    /**
     * Angir det tidligste året som kan påvirke opptjeningen for dette uføretidspunktet.
     */
    var tidligstVurderteAr = 0
    var uforetidspunkt: Date? = null

    /**
     * Dato for når man var sist innmeldt i folketrygden. Benyttes for fremtidig trygdetid.
     */
    var sistMedlTrygden: Date? = null
}
