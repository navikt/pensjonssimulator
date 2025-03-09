package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.ForstegangstjenestetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.ForstegangstjenesteperiodeTypeCti
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import java.util.*

// Checked 2025-02-28
class ForstegangstjenestePeriode {
    var fomDato: Date? = null
    var tomDato: Date? = null
    var periodeType: ForstegangstjenesteperiodeTypeCti? = null
    var periodeTypeEnum: ForstegangstjenestetypeEnum? = null

    //SIMDOM-ADD
    fun ar(): Int? = fomDato?.let(::getYear)

    constructor()

    constructor(source: ForstegangstjenestePeriode) : this() {
        fomDato = source.fomDato?.clone() as? Date
        tomDato = source.tomDato?.clone() as? Date
        source.periodeType?.let { periodeType = ForstegangstjenesteperiodeTypeCti(it) }
        periodeTypeEnum = source.periodeTypeEnum
    }
}
