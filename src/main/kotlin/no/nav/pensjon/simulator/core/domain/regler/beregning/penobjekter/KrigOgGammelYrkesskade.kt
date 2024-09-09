package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * Brukes i PREG kun ved g-omregning
 */
class KrigOgGammelYrkesskade : Ytelseskomponent {

    /**
     * Pensjonsgraden
     */
    var pensjonsgrad: Int = 0

    /**
     * grunnlag for utbetaling;
     */
    var grunnlagForUtbetaling: Int = 0

    /**
     * Kapital utløsning
     */
    var kapitalutlosning: Int = 0

    /**
     * poengtall
     */
    var ps: Double = 0.0

    /**
     * forholdstall yg
     */
    var yg: Double = 0.0

    /**
     * Men del
     */
    var mendel: Int = 0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("KRIG_GY"))

    /**
     * Copy Constructor
     *
     * @param krigOgGammelYrkesskade a `KrigOgGammelYrkesskade` object
     */
    constructor(krigOgGammelYrkesskade: KrigOgGammelYrkesskade) : super(krigOgGammelYrkesskade) {
        pensjonsgrad = krigOgGammelYrkesskade.pensjonsgrad
        grunnlagForUtbetaling = krigOgGammelYrkesskade.grunnlagForUtbetaling
        kapitalutlosning = krigOgGammelYrkesskade.kapitalutlosning
        ps = krigOgGammelYrkesskade.ps
        yg = krigOgGammelYrkesskade.yg
        mendel = krigOgGammelYrkesskade.mendel
    }

    constructor(
        pensjonsgrad: Int,
        grunnlagForUtbetaling: Int,
        kapitalutlosning: Int,
        ps: Double,
        yg: Double,
        mendel: Int
    ) : super(
        ytelsekomponentType = YtelsekomponentTypeCti("KRIG_GY")
    ) {
        this.pensjonsgrad = pensjonsgrad
        this.grunnlagForUtbetaling = grunnlagForUtbetaling
        this.kapitalutlosning = kapitalutlosning
        this.ps = ps
        this.yg = yg
        this.mendel = mendel
    }

    constructor(
        pensjonsgrad: Int = 0,
        grunnlagForUtbetaling: Int = 0,
        kapitalutlosning: Int = 0,
        ps: Double = 0.0,
        yg: Double = 0.0,
        mendel: Int = 0,
        /** super */
        brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("KRIG_GY"),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        fradragsTransaksjon: Boolean = false,
        opphort: Boolean = false,
        sakType: SakTypeCti? = null,
        formelKode: FormelKodeCti? = null,
        reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
        brutto = brutto,
        netto = netto,
        fradrag = fradrag,
        bruttoPerAr = bruttoPerAr,
        nettoPerAr = nettoPerAr,
        fradragPerAr = fradragPerAr,
        ytelsekomponentType = ytelsekomponentType,
        merknadListe = merknadListe,
        fradragsTransaksjon = fradragsTransaksjon,
        opphort = opphort,
        sakType = sakType,
        formelKode = formelKode,
        reguleringsInformasjon = reguleringsInformasjon
    ) {
        this.pensjonsgrad = pensjonsgrad
        this.grunnlagForUtbetaling = grunnlagForUtbetaling
        this.kapitalutlosning = kapitalutlosning
        this.ps = ps
        this.yg = yg
        this.mendel = mendel
    }

}
