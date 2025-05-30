package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent

// 2025-03-18
@JsonSubTypes(
    JsonSubTypes.Type(value = FremskrevetAfpLivsvarig::class),
    JsonSubTypes.Type(value = AfpOffentligLivsvarig::class),
    JsonSubTypes.Type(value = AfpPrivatLivsvarig::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktAfpLivsvarig : Ytelseskomponent()
