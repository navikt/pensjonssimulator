package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import java.io.Serializable

/**
 * Sluttpoengtall
 */
class Sluttpoengtall : Serializable {
    /**
     * sluttpoengtallet
     */
    var pt: Double = 0.0

    /**
     * Obsolete:
     */
    private val pt_eos: Double = 0.0
    private val pt_a10: Double = 0.0

    /**
     * poengtillegg - brukes før 01.04.1984. Legges til poengtallet
     */
    var poengTillegg: Double = 0.0

    /**
     * Liste av merknader.
     */
    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Beregnet gjennomsnittlig SPT*UFG for en som går fra UP over til AP og har
     * EØS-fordeler i UP.
     */
    var fpp_grad_eos: Double = 0.0

    /**
     * Poengrekken som ligger til grunn for sluttpoengtallet.
     */
    var poengrekke: Poengrekke? = null

    constructor(sluttpoengtall: Sluttpoengtall) {
        pt = sluttpoengtall.pt
        poengTillegg = sluttpoengtall.poengTillegg
        for (merknad in sluttpoengtall.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
        fpp_grad_eos = sluttpoengtall.fpp_grad_eos
        if (sluttpoengtall.poengrekke != null) {
            poengrekke = Poengrekke(sluttpoengtall.poengrekke!!)
        }
    }

    constructor(pt: Double, poengTillegg: Double, pt_eos: Double, pt_a10: Double, fpp_grad_eos: Double, poengrekke: Poengrekke) : super() {
        this.pt = pt
        this.poengTillegg = poengTillegg
        this.fpp_grad_eos = fpp_grad_eos
        this.poengrekke = poengrekke
    }

    constructor()
    constructor(pt: Double = 0.0,
                poengTillegg: Double = 0.0,
                merknadListe: MutableList<Merknad> = mutableListOf(),
                fpp_grad_eos: Double = 0.0,
                poengrekke: Poengrekke? = null) {
        this.pt = pt
        this.poengTillegg = poengTillegg
        this.merknadListe = merknadListe
        this.fpp_grad_eos = fpp_grad_eos
        this.poengrekke = poengrekke
    }
}
