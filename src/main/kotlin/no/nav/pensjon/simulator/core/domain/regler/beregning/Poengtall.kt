package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Omsorgsopptjening
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtalltypeEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// 2025-03-10
/**
 * NB: Modified to avoid UnrecognizedPropertyException.
 */
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

    var verdi: Double = 0.0 // = pp

    @Suppress("UNUSED_PARAMETER")
    //Her skal intet skje.
    var justertBelop: Double = 0.0 // always 0.0

    /**
     * Angir om poengtallet er i et omsorgsår
     */
    var omsorg: Boolean = false

    /**
     * Trengs for å implementere Omsorgsopptjening
     */
    @Suppress("UNUSED_PARAMETER")
    //Skal ikke gjøre noe.
    var inntektIAvtaleland: Boolean = false // always false

    var opptjeningsar: Int = 0 // = ar

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
