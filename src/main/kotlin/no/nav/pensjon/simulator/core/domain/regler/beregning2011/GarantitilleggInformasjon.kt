package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti

class GarantitilleggInformasjon {
    var sptVed67: Double = 0.0
    var anvendtSivilstand: BorMedTypeCti? = null
    var pensjonsbeholdningBelopVed67: Double = 0.0
    var garantipensjonsbeholdningBelopVed67: Double = 0.0
    var tt_kapittel20Ved67: Int = 0
    var tt_kapittel19Ved67: Int = 0
    var pa_f92Ved67: Int = 0
    var pa_e91Ved67: Int = 0
    var ftVed67: Double = 0.0
    var dtVed67: Double = 0.0
    var tt_2009: Int = 0
    var pa_f92_2009: Int = 0
    var pa_e91_2009: Int = 0
    var spt_2009: Double = 0.0
    var ft67_1962: Double = 0.0
    var dt67_1962: Double = 0.0

    /**
     * Indikerer om regeltjeneste har estimert trygdetid kapittel 19 basert på
     * trygdetidsgrunnlag for kapittel 20. Denne estimering gjøres i forbindelse med
     * beregning av garantitilleggsbeholdning BER3156. Ref. CR 216411.
     */
    var estimertTTkap19Benyttet: Boolean = false

    constructor() {}

    constructor(gti: GarantitilleggInformasjon) {
        if (gti.anvendtSivilstand != null) {
            anvendtSivilstand = BorMedTypeCti(anvendtSivilstand)
        }
        pensjonsbeholdningBelopVed67 = gti.pensjonsbeholdningBelopVed67
        garantipensjonsbeholdningBelopVed67 = gti.garantipensjonsbeholdningBelopVed67
        tt_kapittel20Ved67 = gti.tt_kapittel20Ved67
        tt_kapittel19Ved67 = gti.tt_kapittel19Ved67
        pa_f92Ved67 = gti.pa_f92Ved67
        pa_e91Ved67 = gti.pa_e91Ved67
        ftVed67 = gti.ftVed67
        dtVed67 = gti.dtVed67
        tt_2009 = gti.tt_2009
        pa_f92_2009 = gti.pa_f92_2009
        pa_e91_2009 = gti.pa_e91_2009
        spt_2009 = gti.spt_2009
        ft67_1962 = gti.ft67_1962
        dt67_1962 = gti.dt67_1962
        sptVed67 = gti.sptVed67
    }

    constructor(
            sptVed67: Double = 0.0,
            anvendtSivilstand: BorMedTypeCti? = null,
            pensjonsbeholdningBelopVed67: Double = 0.0,
            garantipensjonsbeholdningBelopVed67: Double = 0.0,
            tt_kapittel20Ved67: Int = 0,
            tt_kapittel19Ved67: Int = 0,
            pa_f92Ved67: Int = 0,
            pa_e91Ved67: Int = 0,
            ftVed67: Double = 0.0,
            dtVed67: Double = 0.0,
            tt_2009: Int = 0,
            pa_f92_2009: Int = 0,
            pa_e91_2009: Int = 0,
            spt_2009: Double = 0.0,
            ft67_1962: Double = 0.0,
            dt67_1962: Double = 0.0,
            estimertTTkap19Benyttet: Boolean = false
    ) {
        this.sptVed67 = sptVed67
        this.anvendtSivilstand = anvendtSivilstand
        this.pensjonsbeholdningBelopVed67 = pensjonsbeholdningBelopVed67
        this.garantipensjonsbeholdningBelopVed67 = garantipensjonsbeholdningBelopVed67
        this.tt_kapittel20Ved67 = tt_kapittel20Ved67
        this.tt_kapittel19Ved67 = tt_kapittel19Ved67
        this.pa_f92Ved67 = pa_f92Ved67
        this.pa_e91Ved67 = pa_e91Ved67
        this.ftVed67 = ftVed67
        this.dtVed67 = dtVed67
        this.tt_2009 = tt_2009
        this.pa_f92_2009 = pa_f92_2009
        this.pa_e91_2009 = pa_e91_2009
        this.spt_2009 = spt_2009
        this.ft67_1962 = ft67_1962
        this.dt67_1962 = dt67_1962
        this.estimertTTkap19Benyttet = estimertTTkap19Benyttet
    }

}
