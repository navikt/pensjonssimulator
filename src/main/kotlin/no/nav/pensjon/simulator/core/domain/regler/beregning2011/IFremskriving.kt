package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// 2025-06-13
@JsonSubTypes(
    JsonSubTypes.Type(value = FremskrivingsDetaljer::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
interface IFremskriving : IJustering {
    var teller: Double
    var nevner: Double
}
