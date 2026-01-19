package no.nav.pensjon.simulator.core.domain.regler.vedtak

// Copied from pensjon-regler-api 2026-01-16
class Uforegrad : AbstraktBeregningsvilkar() {
    /**
     * Angir Uføregraden UFG.
     */
    var uforegrad = 0

    /**
     * Angir hvilende rett til garantigrad.
     */
    var erGarantigrad = false
}
