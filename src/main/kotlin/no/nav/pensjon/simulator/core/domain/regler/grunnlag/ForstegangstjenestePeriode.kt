package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.ForstegangstjenestetypeEnum
import java.time.LocalDate

// 2026-04-23
class ForstegangstjenestePeriode {
    var fomDatoLd: LocalDate? = null
    var tomDatoLd: LocalDate? = null
    var periodeTypeEnum: ForstegangstjenestetypeEnum? = null

    // Extra:
    fun ar(): Int? = fomDatoLd?.year

    constructor()

    constructor(source: ForstegangstjenestePeriode) : this() {
        fomDatoLd = source.fomDatoLd
        tomDatoLd = source.tomDatoLd
        periodeTypeEnum = source.periodeTypeEnum
    }
}
