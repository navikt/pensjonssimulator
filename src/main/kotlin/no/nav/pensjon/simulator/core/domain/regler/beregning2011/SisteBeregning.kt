package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.io.Serializable
import java.util.*

/**
 * Felles grensesnitt for Siste beregninger slik som SisteBeregning1967, SisteAldersberegning2011, SisteGjenlevendeberegning
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = SisteAldersberegning2011::class),
    JsonSubTypes.Type(value = SisteBeregning1967::class),
    JsonSubTypes.Type(value = SisteUforepensjonBeregning::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class SisteBeregning : Serializable {
    var virkDato: Date? = null
    var tt_anv: Int = 0
    var resultatType: ResultatTypeCti? = null
    var sivilstandType: SivilstandTypeCti? = null
    var benyttetSivilstand: BorMedTypeCti? = null

    protected constructor() : super()

    protected constructor(sb: SisteBeregning) : super() {
        virkDato = sb.virkDato
        tt_anv = sb.tt_anv
        if (sb.resultatType != null) {
            resultatType = ResultatTypeCti(sb.resultatType)
        }
        if (sb.sivilstandType != null) {
            sivilstandType = SivilstandTypeCti(sb.sivilstandType)
        }
        if (sb.benyttetSivilstand != null) {
            benyttetSivilstand = BorMedTypeCti(sb.benyttetSivilstand)
        }
    }

    constructor(
            virkDato: Date? = null,
            tt_anv: Int = 0,
            resultatType: ResultatTypeCti? = null,
            sivilstandType: SivilstandTypeCti? = null,
            benyttetSivilstand: BorMedTypeCti? = null
    ) {
        this.virkDato = virkDato
        this.tt_anv = tt_anv
        this.resultatType = resultatType
        this.sivilstandType = sivilstandType
        this.benyttetSivilstand = benyttetSivilstand
    }

}
