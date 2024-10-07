package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import java.util.*

class ForsteVirkningsdatoGrunnlag {

    var virkningsdato: Date? = null
    var kravFremsattDato: Date? = null
    var bruker: PenPerson? = null
    var annenPerson: PenPerson? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

    constructor()

    constructor(source: ForsteVirkningsdatoGrunnlag) {
        virkningsdato = source.virkningsdato
        kravFremsattDato = source.kravFremsattDato
        bruker = source.bruker
        annenPerson = source.annenPerson
        kravlinjeTypeEnum = source.kravlinjeTypeEnum
    }
}
