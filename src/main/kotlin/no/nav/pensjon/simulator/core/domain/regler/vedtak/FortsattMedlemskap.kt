package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Eksportforbud
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Unntak
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

class FortsattMedlemskap : AbstraktVilkar {
    /**
     * Fortsatt medlem i folketrygden
     */
    var fortsattMedlemFT: Boolean? = null

    /**
     * Brukeren har minst 20 års botid i Norge
     */
    var minstTyveArBotidNorge: Boolean = false

    /**
     * Opptjent rett til tilleggspensjon etter folketrygdloven
     */
    var opptjentRettTilTPEtterFT: Boolean = false

    /**
     * Eksportforbud
     */
    var eksportforbud: Eksportforbud? = null

    /**
     * Fri eksport fordi uførhet skyldes yrkesskade
     */
    var friEksportPgaYrkesskade: Boolean = false

    /**
     * Innvilget garantert tilleggspensjon til ung ufør/død skal eksporteres
     */
    var innvilgetGarantertTP: Boolean = false

    /**
     * Eksportrett garantert TP
     */
    var eksportrettGarantertTP: Unntak? = null

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

    constructor(
        fortsattMedlemFT: Boolean? = null,
        minstTyveArBotidNorge: Boolean = false,
        opptjentRettTilTPEtterFT: Boolean = false,
        eksportforbud: Eksportforbud? = null,
        friEksportPgaYrkesskade: Boolean = false,
        innvilgetGarantertTP: Boolean = false,
        eksportrettGarantertTP: Unntak? = null,
        /** Super */
        resultat: VilkarOppfyltUTCti? = null
    ) : super(resultat) {
        this.fortsattMedlemFT = fortsattMedlemFT
        this.minstTyveArBotidNorge = minstTyveArBotidNorge
        this.opptjentRettTilTPEtterFT = opptjentRettTilTPEtterFT
        this.eksportforbud = eksportforbud
        this.friEksportPgaYrkesskade = friEksportPgaYrkesskade
        this.innvilgetGarantertTP = innvilgetGarantertTP
        this.eksportrettGarantertTP = eksportrettGarantertTP
    }

    override fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar? {
        var fm: FortsattMedlemskap? = null
        if (abstraktVilkar.javaClass == FortsattMedlemskap::class.java) {
            fm = FortsattMedlemskap(abstraktVilkar as FortsattMedlemskap?)
        }
        return fm
    }
}
