package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonSubTypes(
    JsonSubTypes.Type(value = ArbeidsavklaringspengerUT::class),
    JsonSubTypes.Type(value = Arbeidsavklaringspenger::class),
    JsonSubTypes.Type(value = Sykepenger::class),
    JsonSubTypes.Type(value = SykepengerUT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class MotregningYtelseskomponent : BeregningYtelseskomponent() {

    var dagsats = 0
    var antallDager = 0
}
