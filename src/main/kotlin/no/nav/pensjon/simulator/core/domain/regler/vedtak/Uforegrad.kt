package no.nav.pensjon.simulator.core.domain.regler.vedtak

// Copied from pensjon-regler-api v2.4.2 2026-09-04
class Uforegrad : AbstraktBeregningsvilkar() {
    /**
     * Angir Uføregraden UFG.
     */
    var uforegrad = 0

    /**
     * Angir hvilende rett til garantigrad.
     */
    var erGarantigrad = false

    /**
     * Angir om uføregrad har økt uten at restarbeidsevne er vurdert
     */
    var øktUføregradUtenVurderingAvRestarbeidsevne = false
}