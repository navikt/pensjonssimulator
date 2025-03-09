package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtalltypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.PoengtallTypeCti

class Poengtall : Omsorgsopptjening {
    /**
     * Pensjonspoeng,
     */
    @JvmField
    var pp = 0.0

    /**
     * Anvendt pensjonsgivende inntekt.
     */
    var pia = 0

    /**
     * Pensjonsgivende inntekt.
     */
    var pi = 0

    /**
     * Året for dette poengtallet
     */
    var ar = 0

    /**
     * Angir om poengtallet er brukt i beregningen av sluttpoengtall.
     */
    var bruktIBeregning = false

    /**
     * Veiet grunnbeløp
     */
    var gv = 0

    /**
     * Poengtalltype.
     */
    var poengtallType: PoengtallTypeCti? = null
    var poengtallTypeEnum: PoengtalltypeEnum? = null

    /**
     * Maks Uføregrad for dette året
     */
    var maksUforegrad = 0

    /**
     * Angir om året er et Uføreår.
     */
    var uforear = false

    var merknadListe: MutableList<Merknad> = mutableListOf()

    override val verdi: Double
        get() = pp

    @Suppress("UNUSED_PARAMETER")
    //Her skal intet skje.
    override var justertBelop: Double
        get() = 0.0
        set(justertBelop) {}

    /**
     * Angir om poengtallet er i et omsorgsår
     */
    override var omsorg: Boolean = false
    /**
     * Trengs for å implementere Omsorgsopptjening
     */
    @Suppress("UNUSED_PARAMETER")
    //Skal ikke gjøre noe.
    override var inntektIAvtaleland: Boolean
        get() = false
        set(inntektIAvtaleland) {}

    override val opptjeningsar: Int
        get() = ar

    constructor()
    constructor(poengtall: Poengtall) {
        pp = poengtall.pp
        pia = poengtall.pia
        pi = poengtall.pi
        ar = poengtall.ar
        bruktIBeregning = poengtall.bruktIBeregning
        gv = poengtall.gv
        if (poengtall.poengtallType != null) {
            poengtallType = PoengtallTypeCti(poengtall.poengtallType!!)
        }
        if (poengtall.poengtallTypeEnum != null) {
            poengtallTypeEnum = poengtall.poengtallTypeEnum
        }
        maksUforegrad = poengtall.maksUforegrad
        uforear = poengtall.uforear
        merknadListe = ArrayList()
        for (merknad in poengtall.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
    }
}
