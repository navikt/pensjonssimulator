package no.nav.pensjon.simulator.core.domain.regler.vedtak

// Copied from pensjon-regler-api v2.0.0 2026-06-04
class VilkarsprovInformasjon2016 : VilkarsprovInformasjon() {
    var vektetPensjonsniva = 0.0
    var vektetPensjonsnivaProRata = 0.0
    var vilkarsprovInformasjon2011: VilkarsprovInformasjon2011? = null
    var vilkarsprovInformasjon2025: VilkarsprovInformasjon2025? = null
}