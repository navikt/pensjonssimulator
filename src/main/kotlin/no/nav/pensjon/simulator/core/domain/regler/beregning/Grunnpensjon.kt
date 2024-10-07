package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BasisGrunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GPSatsTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

@JsonSubTypes(
    JsonSubTypes.Type(value = BasisGrunnpensjon::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class Grunnpensjon : Ytelseskomponent {
    /**
     * Prosentsatsen.
     */
    var pSats_gp = 0.0

    /**
     * Ordinår, forhøyet
     */
    var satsTypeEnum: GPSatsTypeEnum? = null

    var ektefelleInntektOver2G = false

    /**
     * Trygdetid anvendt i beregning av grunnpensjon.
     */
    var anvendtTrygdetid: AnvendtTrygdetid? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.GP

    constructor() {
        formelKodeEnum = FormelKodeEnum.GPx
    }

    /**
     * Copy Constructor
     */
    constructor(source: Grunnpensjon) : super(source) {
        pSats_gp = source.pSats_gp

        if (source.satsTypeEnum != null) {
            satsTypeEnum = source.satsTypeEnum
        }

        ektefelleInntektOver2G = source.ektefelleInntektOver2G

        if (source.anvendtTrygdetid != null) {
            anvendtTrygdetid = AnvendtTrygdetid(source.anvendtTrygdetid!!)
        }
    }
}
