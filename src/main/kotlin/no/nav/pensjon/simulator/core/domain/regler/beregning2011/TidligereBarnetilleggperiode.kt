package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// 2025-06-06
class TidligereBarnetilleggperiode : AbstraktBarnetilleggperiode() {
    /**
     * Hva barnetillegget i tidligere periode faktisk ble avkortet med per år.
     */
    var faktiskFradragPerAr = 0.0

    /**
     * Periodens bidrag til avviksbeløp.
     */
    var avviksbelop = 0.0
}
