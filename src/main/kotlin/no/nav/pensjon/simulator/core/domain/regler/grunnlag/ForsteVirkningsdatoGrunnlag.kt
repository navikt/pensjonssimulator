package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import java.util.*

class ForsteVirkningsdatoGrunnlag {

    var virkningsdato: Date? = null
    var kravFremsattDato: Date? = null
    var bruker: PenPerson? = null
    var annenPerson: PenPerson? = null

    var kravlinjeType: KravlinjeTypeCti? = null //TODO remove
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null
        get() {
            return field ?: kravlinjeType?.let { KravlinjeTypeEnum.valueOf(it.kode) }
        }
        set(value) {
            field = value
            kravlinjeType = value?.let { KravlinjeTypeCti(it.name).apply { hovedKravlinje = it.erHovedkravlinje } }
        }

    constructor()

    constructor(source: ForsteVirkningsdatoGrunnlag) {
        virkningsdato = source.virkningsdato
        kravFremsattDato = source.kravFremsattDato
        bruker = source.bruker
        annenPerson = source.annenPerson
        kravlinjeTypeEnum = source.kravlinjeTypeEnum
        kravlinjeType = source.kravlinjeType
    }
}
