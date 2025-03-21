package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.OpptjeningUT

/**
 * Converted from interface to class to avoid UnrecognizedPropertyException in subtypes.
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = Poengtall::class),
    JsonSubTypes.Type(value = OpptjeningUT::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class Omsorgsopptjening

