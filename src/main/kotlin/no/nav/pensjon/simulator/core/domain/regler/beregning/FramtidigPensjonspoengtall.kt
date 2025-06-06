package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// 2025-06-06
/**
* Framtidig pensjonspoengtall.
* Brukes ved beregning av uførepensjon,gjenlevendepensjon og AFP.
*/
class FramtidigPensjonspoengtall {
    /**
     * Poengtallet
     */
    var pt = 0.0

    /**
     * Poengtallene som ligger til grunn for poengtallet.
     */
    var poengtallListe: List<Poengtall> = mutableListOf()

    /**
     * Liste av merknader.
     */
    var merknadListe: List<Merknad> = mutableListOf()

    constructor()

    constructor(framtidigPensjonspoengtall: FramtidigPensjonspoengtall) {
        pt = framtidigPensjonspoengtall.pt
        poengtallListe = mutableListOf()
        for (poengtall in framtidigPensjonspoengtall.poengtallListe) {
            (poengtallListe as MutableList<Poengtall>).add(Poengtall(poengtall))
        }
        merknadListe = ArrayList()
        for (merknad in framtidigPensjonspoengtall.merknadListe) {
            (merknadListe as ArrayList<Merknad>).add(merknad.copy())
        }
    }
}
