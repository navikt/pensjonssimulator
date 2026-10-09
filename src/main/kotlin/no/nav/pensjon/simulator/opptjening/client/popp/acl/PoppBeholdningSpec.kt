package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.opptjening.BeholdningSpec
import java.util.*

data class PoppBeholdningSpec(
    val fnr: String? = null,
    val beholdningType: String? = null,
    val serviceDirectiveTPOPP006: String? = null,
    val fomDato: Date? = null,
    val tomDato: Date? = null
) {
    companion object {
        fun fromInternalValue(source: BeholdningSpec) =
            PoppBeholdningSpec(
                fnr = source.pid.value,
                beholdningType = source.beholdningType.name,
                serviceDirectiveTPOPP006 = source.serviceDirektiv,
                fomDato = source.fom?.toNorwegianDate(),
                tomDato = source.tom?.toNorwegianDate()
            )
    }
}