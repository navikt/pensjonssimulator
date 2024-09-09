package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.io.Serializable
import java.util.*

class ForsteVirkningsdatoGrunnlag : Serializable, Comparable<ForsteVirkningsdatoGrunnlag> {
    var virkningsdato: Date? = null
    var kravFremsattDato: Date? = null
    var bruker: PenPerson? = null
    var annenPerson: PenPerson? = null
    var kravlinjeType: KravlinjeTypeCti? = null

    constructor(forsteVirkningsdatoGrunnlag: ForsteVirkningsdatoGrunnlag) {
        virkningsdato = forsteVirkningsdatoGrunnlag.virkningsdato
        kravFremsattDato = forsteVirkningsdatoGrunnlag.kravFremsattDato
        bruker = forsteVirkningsdatoGrunnlag.bruker
        annenPerson = forsteVirkningsdatoGrunnlag.annenPerson
        kravlinjeType = forsteVirkningsdatoGrunnlag.kravlinjeType
    }

    constructor(virkningsdato: Date? = null,
                kravFremsattDato: Date? = null,
                bruker: PenPerson? = null,
                annenPerson: PenPerson? = null,
                kravlinjeType: KravlinjeTypeCti? = null) {
        this.virkningsdato = virkningsdato
        this.kravFremsattDato = kravFremsattDato
        this.bruker = bruker
        this.annenPerson = annenPerson
        this.kravlinjeType = kravlinjeType
    }

    // Får feil i Jackson-mapping ved å ikke ha denne...
    constructor()

    override fun compareTo(other: ForsteVirkningsdatoGrunnlag): Int {
        return DateCompareUtil.compareTo(kravFremsattDato, other.kravFremsattDato)
    }

    companion object {
        private const val serialVersionUID = 8376138604433396886L
    }
}
