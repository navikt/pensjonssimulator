package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.MinstePensjonsnivaSatsEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

@JsonSubTypes(
    JsonSubTypes.Type(value = BasisPensjonstillegg::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class Pensjonstillegg : Ytelseskomponent {

    var forholdstall67 = 0.0
    var minstepensjonsnivaSats = 0.0
    var minstepensjonsnivaSatsTypeEnum: MinstePensjonsnivaSatsEnum? = null
    var justertMinstePensjonsniva: JustertMinstePensjonsniva? = null

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.PT

    constructor() {
        formelKodeEnum = FormelKodeEnum.PenTx
    }

    constructor(source: Pensjonstillegg) : super(source) {
        forholdstall67 = source.forholdstall67
        minstepensjonsnivaSats = source.minstepensjonsnivaSats

        if (source.minstepensjonsnivaSatsTypeEnum != null) {
            minstepensjonsnivaSatsTypeEnum = source.minstepensjonsnivaSatsTypeEnum
        }

        if (source.justertMinstePensjonsniva != null) {
            justertMinstePensjonsniva = JustertMinstePensjonsniva(source.justertMinstePensjonsniva!!)
        }
    }
}
