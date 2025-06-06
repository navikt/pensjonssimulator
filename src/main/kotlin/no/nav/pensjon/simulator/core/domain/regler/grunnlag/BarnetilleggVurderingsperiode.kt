package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

// 2025-06-06
class BarnetilleggVurderingsperiode {
    var fomDato: Date? = null
    var tomDato: Date? = null
    var btVilkarListe: List<BarnetilleggVilkar> = mutableListOf()
}
