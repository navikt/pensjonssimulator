package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import java.io.Serializable
import java.util.*

/**
 * Framtidig pensjonspoengtall.
 * Brukes ved beregning av uførepensjon,gjenlevendepensjon og AFP.
 */
class FramtidigPensjonspoengtall : Serializable {

    /**
     * Poengtallet
     */
    var pt: Double = 0.0

    /**
     * Poengtallene som ligger til grunn for poengtallet.
     */
    var poengtallListe: MutableList<Poengtall> = mutableListOf()

    /**
     * Liste av merknader.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Omregnet poengtall etter 1991. Intern PREG variabel.
     */
    @JsonIgnore
    var pt_e91: Double = 0.0

    constructor() : super()

    constructor(framtidigPensjonspoengtall: FramtidigPensjonspoengtall) {
        pt = framtidigPensjonspoengtall.pt
        for (poengtall in framtidigPensjonspoengtall.poengtallListe) {
            poengtallListe.add(Poengtall(poengtall))
        }
        merknadListe = framtidigPensjonspoengtall.merknadListe.map { it.copy() }.toMutableList()
    }

    constructor(
        pt: Double = 0.0,
        poengtallListe: MutableList<Poengtall> = mutableListOf(),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        pt_e91: Double = 0.0
    ) {
        this.pt = pt
        this.poengtallListe = poengtallListe
        this.merknadListe = merknadListe
        this.pt_e91 = pt_e91
    }

    /**
     * Constructs a `String` with all attributes
     * in name = value format.
     *
     * @return a `String` representation
     * of this object.
     */
    override fun toString(): String {
        val TAB = "    "
        val retValue = StringBuilder()
        retValue.append("FramtidigPensjonspoengtall ( ").append(super.toString()).append(TAB).append("pt = ").append(pt)
            .append(TAB).append("poengtallListe = ")
            .append(poengtallListe).append(TAB).append("merknadListe = ").append(merknadListe).append(TAB).append(" )")
        return retValue.toString()
    }

    fun sortertPoengtallListe(): MutableList<Poengtall> {
        val sortedPt = ArrayList(poengtallListe)
        Collections.sort(sortedPt, Collections.reverseOrder())
        return sortedPt
    }
}
