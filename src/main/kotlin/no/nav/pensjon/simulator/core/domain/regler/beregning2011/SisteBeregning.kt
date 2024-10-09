package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultattypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
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
abstract class SisteBeregning protected constructor() {

    var virkDato: Date? = null
    var tt_anv = 0
    var resultatTypeEnum: ResultattypeEnum? = null
    var sivilstandTypeEnum: SivilstandEnum? = null
    var benyttetSivilstandEnum: BorMedTypeEnum? = null
}
