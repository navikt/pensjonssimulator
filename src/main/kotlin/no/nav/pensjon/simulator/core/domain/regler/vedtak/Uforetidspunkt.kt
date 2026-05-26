package no.nav.pensjon.simulator.core.domain.regler.vedtak

import java.time.LocalDate

// 2026-05-05
class Uforetidspunkt : AbstraktBeregningsvilkar() {
    /**
     * Angir det tidligste året som kan påvirke opptjeningen for dette uføretidspunktet.
     */
    var tidligstVurderteAr = 0

    var uforetidspunktLd: LocalDate? = null

    /**
     * Dato for når man var sist innmeldt i folketrygden. Benyttes for fremtidig trygdetid.
     */
    var sistMedlTrygdenLd: LocalDate? = null
}