package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-20
class EktefelletilleggUT : Ytelseskomponent(), UforetrygdYtelseskomponent {
    /**
     * årsbeløp for delytelsen fra tidligere vedtak (fra tilsvarende beregningsperiode)
     */
    override var tidligereBelopAr = 0

    /**
     * Akkumulert netto hittil i året eksklusiv måned for beregningsperiodens fomDato.
     */
    var nettoAkk = 0

    /**
     * gjenstående beløp brukeren har rett på for året som beregningsperioden starter,
     * og inkluderer måneden det beregnes fra.
     */
    var nettoRestAr = 0

    /**
     * Inntektsavkortningsbeløp per år, før justering med differansebeløp
     */
    var avkortningsbelopPerAr = 0

    /**
     * netto ektefelletillegg per måned før konvertering * 12
     */
    var etForSkattekomp = 0.0

    /**
     * brukers oppjusterte uførepensjon før skattekompensasjon
     */
    var upForSkattekomp = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.UT_ET
}
