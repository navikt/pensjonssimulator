package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import java.util.*

// Checked 2025-02-28
class Sivilstand {
    /**
     * Kode som angir hvilken type sivilstand personen har.
     */
    var sivilstandTypeEnum: SivilstandEnum? = null

    /**
     * Sivilstandens gyldighet fra-og-med dato.
     */
    var fom: Date? = null

    /**
     * Sivilstandens gyldighet til-og-med dato
     */
    var tom: Date? = null

    /**
     * Person sivilstanden refererer seg til, for eksempel ektefellen hvis *sivilstandType*="Ektefelle"
     */
    var relatertPerson: PenPerson? = null

    /**
     * Angir sivilstandens kilde.
     */
    var kildeEnum: GrunnlagkildeEnum? = null

    constructor()

    constructor(source: Sivilstand) : this() {
        sivilstandTypeEnum = source.sivilstandTypeEnum
        fom = source.fom?.clone() as? Date
        tom = source.tom?.clone() as? Date
        source.relatertPerson?.let { relatertPerson = PenPerson(it) }
        kildeEnum = source.kildeEnum
    }
}
