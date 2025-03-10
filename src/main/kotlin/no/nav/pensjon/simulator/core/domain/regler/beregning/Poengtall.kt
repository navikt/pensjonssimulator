package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtalltypeEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// 2025-03-10
class Poengtall : Omsorgsopptjening {
    /**
     * Pensjonspoeng,
     */
    var pp: Double = 0.0

    /**
     * Anvendt pensjonsgivende inntekt.
     */
    var pia = 0

    /**
     * Pensjonsgivende inntekt.
     */
    var pi = 0

    /**
     * året for dette poengtallet
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

    constructor(source: Poengtall) {
        pp = source.pp
        pia = source.pia
        pi = source.pi
        ar = source.ar
        bruktIBeregning = source.bruktIBeregning
        gv = source.gv
        poengtallTypeEnum = source.poengtallTypeEnum
        maksUforegrad = source.maksUforegrad
        uforear = source.uforear
        merknadListe = source.merknadListe.map { it.copy() }.toMutableList()
    }
}
