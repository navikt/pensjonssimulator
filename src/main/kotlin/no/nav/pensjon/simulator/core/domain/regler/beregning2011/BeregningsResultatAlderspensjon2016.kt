package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.io.Serializable
import java.util.*

class BeregningsResultatAlderspensjon2016 : Serializable, AbstraktBeregningsResultat {

    var andelKapittel19: Int = 0
    var beregningsResultat2011: BeregningsResultatAlderspensjon2011? = null
    var beregningsResultat2025: BeregningsResultatAlderspensjon2025? = null

    constructor() : super()

    constructor(br: BeregningsResultatAlderspensjon2016) : super(br) {
        if (br.beregningsResultat2011 != null) {
            beregningsResultat2011 = BeregningsResultatAlderspensjon2011(br.beregningsResultat2011!!)
        }
        if (br.beregningsResultat2025 != null) {
            beregningsResultat2025 = BeregningsResultatAlderspensjon2025(br.beregningsResultat2025!!)
        }
        andelKapittel19 = br.andelKapittel19
    }

    constructor(
        andelKapittel19: Int = 0,
        beregningsResultat2011: BeregningsResultatAlderspensjon2011? = null,
        beregningsResultat2025: BeregningsResultatAlderspensjon2025? = null,
        /** super AbstraktBeregningsResultat */
            virkFom: Date? = null,
        pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
        uttaksgrad: Int = 0,
        brukersSivilstand: SivilstandTypeCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        beregningArsak: BeregningArsakCti? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        gjennomsnittligUttaksgradSisteAr: Double = 0.0
    ) : super(
            virkFom = virkFom,
            pensjonUnderUtbetaling = pensjonUnderUtbetaling,
            uttaksgrad = uttaksgrad,
            brukersSivilstand = brukersSivilstand,
            benyttetSivilstand = benyttetSivilstand,
            beregningArsak = beregningArsak,
            lonnsvekstInformasjon = lonnsvekstInformasjon,
            merknadListe = merknadListe,
            gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
    ) {
        this.andelKapittel19 = andelKapittel19
        this.beregningsResultat2011 = beregningsResultat2011
        this.beregningsResultat2025 = beregningsResultat2025
    }
}
