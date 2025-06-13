package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

// 2025-06-13
@JsonSubTypes(
    JsonSubTypes.Type(value = IFremskriving::class),
    JsonSubTypes.Type(value = ILonnsvekst::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
interface IJustering {
    var justeringsfaktor: Double
    var justeringTomDato: Date?
}
