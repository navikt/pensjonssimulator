package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.xml.bind.annotation.XmlAccessType.FIELD
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlType

@XmlAccessorType(FIELD)
@XmlType(name = "", propOrder = ["fnr"])

data class FNR(
        @get:JsonValue val fnr: String? = null
) {
    override fun toString() = fnr ?: "fnr er null"
}