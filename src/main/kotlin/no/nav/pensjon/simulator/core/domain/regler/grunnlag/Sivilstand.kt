package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.util.*

class Sivilstand(

    /**
     * Kode som angir hvilken type sivilstand personen har.
     */
    var sivilstandType: SivilstandTypeCti? = null,

    /**
     * Sivilstandens gyldighet fra-og-med dato.
     */
    var fom: Date? = null,

    /**
     * Sivilstandens gyldighet til-og-med dato
     */
    var tom: Date? = null,

    /**
     * Person sivilstanden refererer seg til, for eksempel ektefellen hvis *sivilstandType*="Ektefelle"
     */
    var relatertPerson: PenPerson? = null,

    /**
     * Angir sivilstandens kilde.
     */
    var kilde: GrunnlagKildeCti? = null

) {
    constructor(sivilstand: Sivilstand) : this() {
        if (sivilstand.sivilstandType != null) {
            this.sivilstandType = SivilstandTypeCti(sivilstand.sivilstandType)
        }
        if (sivilstand.fom != null) {
            this.fom = sivilstand.fom!!.clone() as Date
        }
        if (sivilstand.tom != null) {
            this.tom = sivilstand.tom!!.clone() as Date
        }
        if (sivilstand.relatertPerson != null) {
            this.relatertPerson = PenPerson(sivilstand.relatertPerson!!)
        }
        if (sivilstand.kilde != null) {
            this.kilde = GrunnlagKildeCti(sivilstand.kilde)
        }
    }
}
