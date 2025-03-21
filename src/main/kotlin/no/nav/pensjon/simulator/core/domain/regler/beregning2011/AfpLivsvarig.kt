package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-10
// pensjon-regler-api: no/nav/pensjon/regler/domain/beregning2011/AfpLivsvarig.kt
@JsonSubTypes(
    JsonSubTypes.Type(value = FremskrevetAfpLivsvarig::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class AfpLivsvarig : Ytelseskomponent {
    var justeringsbelop = 0
    var afpProsentgrad = 0.0
    var afpForholdstall = 0.0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.AFP_LIVSVARIG

    constructor() {
        formelKodeEnum = FormelKodeEnum.AFPx
    }

    constructor(source: AfpLivsvarig) : super(source) {
        afpForholdstall = source.afpForholdstall
        afpProsentgrad = source.afpProsentgrad
        justeringsbelop = source.justeringsbelop
    }

    // SIMDOM-ADD:
    constructor(
        formelKode: FormelKodeEnum,
        bruttoPerAr: Double = 0.0
    ) {
        this.formelKodeEnum = formelKode
        this.bruttoPerAr = bruttoPerAr
    }
    // end SIMDOM-ADD
}
