package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.util.Date

@JsonSubTypes(
    JsonSubTypes.Type(value = IGRegulering::class),
    JsonSubTypes.Type(value = IFremskriving::class),
    JsonSubTypes.Type(value = ILonnsvekst::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
interface IJustering : Serializable {
    var justeringsfaktor: Double
    var justeringTomDato: Date?
}
