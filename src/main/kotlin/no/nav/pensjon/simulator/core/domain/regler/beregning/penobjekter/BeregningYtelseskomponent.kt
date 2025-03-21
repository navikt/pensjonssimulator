package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent

// 2025-03-19
@JsonSubTypes(
    JsonSubTypes.Type(value = MotregningYtelseskomponent::class),
    JsonSubTypes.Type(value = SkattefriGrunnpensjon::class),
    JsonSubTypes.Type(value = SkattefriUforetrygdOrdiner::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class BeregningYtelseskomponent : Ytelseskomponent() {
    // Benyttes i PEN domenemodell for MapKey in Beregning
    private var ytelseKomponentTypeName: String? = null
    protected var beregning: Beregning? = null
}
