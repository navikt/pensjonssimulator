package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.YrkeYrkesskadeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.YrkeYrkesskadeCti
import java.util.*

/**
 * Grunnlagsdata for yrkesskade. Denne er et tillegg til Uforegrunnlag. Hvis
 * dette objektet finnes (!=null) må det finnes et Uforegrunnlag. Dersom dette
 * objektet er null og Uforegrunnlaget != null betyr det at personen ikke har
 * yrkesskade.
 */
// Checked 2025-02-28
class Yrkesskadegrunnlag {
    /**
     * Dato for skadetidspunkt.
     */
    var yst: Date? = null

    /**
     * Uføregrad ved yrkesskade, heltall 0-100.Kan ikke være større enn
     * uføregraden(ufg).For avdød pga yrkesskade settes yug til 100.
     */
    var yug = 0

    /**
     * Angir om yrkesskaden skyldes yrkessykdom.
     */
    var yrkessykdom = false

    /**
     * Det er en minimumsgaranti mht poengtall for som gjelder for spesielle
     * yrkesgrupper, f.eks fiskere,fangstmenn, militære,ungdom under utdanning
     * osv.
     */
    var yrke: YrkeYrkesskadeCti? = null
    var yrkeEnum: YrkeYrkesskadeEnum? = null

    /**
     * Bruker forsørget av avdød iht paragraf 17-12.2
     */
    var brukerForsorgetAvAnnen = false

    /**
     * Antatt årlig inntekt på skadetidspunktet.
     */
    var antattArligInntekt = 0

    /**
     * Angir om yrkesskadegrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true

    constructor()

    constructor(source: Yrkesskadegrunnlag) : this() {
        yst = source.yst?.clone() as? Date
        yug = source.yug
        yrkessykdom = source.yrkessykdom
        yrke = source.yrke?.let(::YrkeYrkesskadeCti)
        brukerForsorgetAvAnnen = source.brukerForsorgetAvAnnen
        antattArligInntekt = source.antattArligInntekt
        bruk = source.bruk
    }
}
