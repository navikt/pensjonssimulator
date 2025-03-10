package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Eksportforbud
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Unntak

// 2025-03-10
class FortsattMedlemskap : AbstraktVilkar() {
    /**
     * Fortsatt medlem i folketrygden
     */
    var fortsattMedlemFT: Boolean? = null

    /**
     * Brukeren har minst 20 års botid i Norge
     */
    var minstTyveArBotidNorge: Boolean? = null

    /**
     * Opptjent rett til tilleggspensjon etter folketrygdloven
     */
    var opptjentRettTilTPEtterFT: Boolean? = null

    /**
     * Eksportforbud
     */
    var eksportforbud: Eksportforbud? = null

    /**
     * Fri eksport fordi uførhet skyldes yrkesskade
     */
    var friEksportPgaYrkesskade: Boolean? = null

    /**
     * Innvilget garantert tilleggspensjon til ung ufør/død skal eksporteres
     */
    var innvilgetGarantertTP: Boolean? = null

    /**
     * Eksportrett garantert TP
     */
    var eksportrettGarantertTP: Unntak? = null
/*
    constructor() : super()

    constructor(aFortsattMedlemskap: FortsattMedlemskap?) : super(aFortsattMedlemskap!!) {
        if (aFortsattMedlemskap.fortsattMedlemFT != null) {
            this.fortsattMedlemFT = aFortsattMedlemskap.fortsattMedlemFT
        }
        this.minstTyveArBotidNorge = aFortsattMedlemskap.minstTyveArBotidNorge
        this.opptjentRettTilTPEtterFT = aFortsattMedlemskap.opptjentRettTilTPEtterFT
        if (aFortsattMedlemskap.eksportforbud != null) {
            this.eksportforbud = Eksportforbud(aFortsattMedlemskap.eksportforbud!!)
        }
        this.friEksportPgaYrkesskade = aFortsattMedlemskap.friEksportPgaYrkesskade
        this.innvilgetGarantertTP = aFortsattMedlemskap.innvilgetGarantertTP
        if (aFortsattMedlemskap.eksportrettGarantertTP != null) {
            this.eksportrettGarantertTP = Unntak(aFortsattMedlemskap.eksportrettGarantertTP!!)
        }
    }
*/
}
