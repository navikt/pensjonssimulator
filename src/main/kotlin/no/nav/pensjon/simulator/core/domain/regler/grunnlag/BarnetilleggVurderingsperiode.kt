package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.time.LocalDate

// 2026-04-23
class BarnetilleggVurderingsperiode {
    var fomDatoLd: LocalDate? = null
    var tomDatoLd: LocalDate? = null
    var btVilkarListe: List<BarnetilleggVilkar> = mutableListOf()
}
