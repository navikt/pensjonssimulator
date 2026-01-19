package no.nav.pensjon.simulator.core.domain.regler.vedtak

// Copied from pensjon-regler-api 2026-01-16
class TidligereGjenlevendePensjon : AbstraktBeregningsvilkar() {
    /**
     * Angir om bruker mottok GJP som fålge av avdødes dødsfall.
     */
    var sokerMottokGJPForAvdod = false

    /**
     * Angir om avdøde hadde inntekt på minst 1G før dødsfall
     */
    var arligPGIMinst1G = false
}
