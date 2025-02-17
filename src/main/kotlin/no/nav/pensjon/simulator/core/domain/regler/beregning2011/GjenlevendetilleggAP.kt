package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.GjenlevendetilleggAPkap20Enum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.util.formula.Formel
import no.nav.pensjon.simulator.core.domain.regler.util.formula.IFormelProvider

class GjenlevendetilleggAP : Ytelseskomponent, IFormelProvider {

    /**
     * Sum av GP, TP og PenT for AP2011 medregnet GJR.
     */
    var apKap19MedGJR = 0

    /**
     * Sum av GP, TP og PenT for AP2011 uten GJR.
     */
    var apKap19UtenGJR = 0

    /**
     * Differanse mellom AP med og uten GJR, justert til 100% uttaksgrad.
     */
    var referansebelop: Int = 0

    /**
     * Summen av reguleringsbeløp som har gått i fradrag på gjenlevendetillegget
     */
    var sumReguleringsfradrag: Int = 0

    /**
     * Det maksimale uttaksgrad som kan benyttes ved beregning av gjenlevendetillegget.
     */
    var anvendtUttaksgrad: Int = 100

    /**
     * Den beregningstekniske metoden som er benyttet for å fastsette gjenlevendetillegget.
     */
    var metode: GjenlevendetilleggAPkap20Enum = GjenlevendetilleggAPkap20Enum.INGEN

    /**
     * Map av formler brukt i beregning av ytelseskomponenten.
     */
    override var formelMap: HashMap<String, Formel> = HashMap()

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AP_GJT

    // SIMDOM-ADD
    constructor() : super(typeEnum = YtelseskomponentTypeEnum.AP_GJT)

    /**
     * Used via reflection in PensjonUnderUtbetaling: constructor.newInstance(komponent)
     */
    constructor(source: GjenlevendetilleggAP) : super(source) {
        ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.AP_GJT
        apKap19MedGJR = source.apKap19MedGJR
        apKap19UtenGJR = source.apKap19UtenGJR
        referansebelop = source.referansebelop
        sumReguleringsfradrag = source.sumReguleringsfradrag
        anvendtUttaksgrad = source.anvendtUttaksgrad
        metode = source.metode
        source.formelMap.forEach { (key, value) -> formelMap[key] = Formel(value) }
    }
    // end SIMDOM-ADD
}
