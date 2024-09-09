package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.YrkeYrkesskadeCti
import java.io.Serializable
import java.util.*

/**
 * Grunnlagsdata for yrkesskade. Denne er et tillegg til Uforegrunnlag. Hvis
 * dette objektet finnes (!=null) må det finnes et Uforegrunnlag. Dersom dette
 * objektet er null og Uforegrunnlaget != null betyr det at personen ikke har
 * yrkesskade.
 */
class Yrkesskadegrunnlag(

    /**
     * Dato for skadetidspunkt.
     */
    var yst: Date? = null,

    /**
     * Uføregrad ved yrkesskade, heltall 0-100.Kan ikke være større enn
     * uføregraden(ufg).For avdød pga yrkesskade settes yug til 100.
     */
    var yug: Int = 0,

    /**
     * Angir om yrkesskaden skyldes yrkessykdom.
     */
    var yrkessykdom: Boolean = false,

    /**
     * Det er en minimumsgaranti mht poengtall for som gjelder for spesielle
     * yrkesgrupper, f.eks fiskere,fangstmenn, militære,ungdom under utdanning
     * osv.
     */
    var yrke: YrkeYrkesskadeCti? = null,

    /**
     * Bruker forsørget av avdød iht paragraf 17-12.2
     */
    var brukerForsorgetAvAvdod: Boolean = false,

    /**
     * Antatt årlig inntekt på skadetidspunktet.
     */
    var antattArligInntekt: Int = 0,

    /**
     * Angir om yrkesskadegrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = false
) : Serializable {

    constructor(yrkesskadegrunnlag: Yrkesskadegrunnlag) : this() {
        if (yrkesskadegrunnlag.yst != null) {
            this.yst = yrkesskadegrunnlag.yst!!.clone() as Date
        }
        this.yug = yrkesskadegrunnlag.yug
        this.yrkessykdom = yrkesskadegrunnlag.yrkessykdom
        if (yrkesskadegrunnlag.yrke != null) {
            this.yrke = YrkeYrkesskadeCti(yrkesskadegrunnlag.yrke)
        }
        this.brukerForsorgetAvAvdod = yrkesskadegrunnlag.brukerForsorgetAvAvdod
        this.antattArligInntekt = yrkesskadegrunnlag.antattArligInntekt
        this.bruk = yrkesskadegrunnlag.bruk
    }

    constructor(
        yst: Date,
        yug: Int,
        yrkessykdom: Boolean,
        yrke: YrkeYrkesskadeCti,
        brukerForsorgetAvAvdod: Boolean,
        antattArligInntekt: Int
    ) : this() {
        this.yst = yst
        this.yug = yug
        this.yrkessykdom = yrkessykdom
        this.yrke = yrke
        this.brukerForsorgetAvAvdod = brukerForsorgetAvAvdod
        this.antattArligInntekt = antattArligInntekt
        this.bruk = true
    }
}
