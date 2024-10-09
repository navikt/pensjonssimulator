package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

@JsonSubTypes(
    JsonSubTypes.Type(value = FremskrevetAfpLivsvarig::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class AfpLivsvarig : Ytelseskomponent {

    var justeringsbelop = 0
    var afpProsentgrad = 0.0
    var afpForholdstall = 0.0

    override lateinit var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum

    constructor() {
        formelKodeEnum = FormelKodeEnum.AFPx
    }

    constructor(
        //ytelsekomponentType: YtelseskomponentTypeEnum,
        formelKode: FormelKodeEnum,
        bruttoPerAr: Double = 0.0
    ) {
        //this.ytelsekomponentTypeEnum = ytelsekomponentType
        this.formelKodeEnum = formelKode
        this.bruttoPerAr = bruttoPerAr
    }

    constructor(source: AfpLivsvarig) : super(source) {
        afpForholdstall = source.afpForholdstall
        afpProsentgrad = source.afpProsentgrad
        justeringsbelop = source.justeringsbelop
    }
}
