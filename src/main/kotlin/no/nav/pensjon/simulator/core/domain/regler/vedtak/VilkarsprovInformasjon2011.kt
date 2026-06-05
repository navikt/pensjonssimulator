package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*

// Copied from pensjon-regler-api v2.0.0 2026-06-04
class VilkarsprovInformasjon2011 : VilkarsprovInformasjon() {
    var mpn67: JustertMinstePensjonsniva? = null
    var mpn67ProRata: JustertMinstePensjonsniva? = null
}