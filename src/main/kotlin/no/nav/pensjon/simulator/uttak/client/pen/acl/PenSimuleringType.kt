package no.nav.pensjon.simulator.uttak.client.pen.acl

import no.nav.pensjon.simulator.simulering.SimuleringType

/**
 * The 'externalValue' is simulering-type values used by PEN (pensjonsfaglig kjerne).
 * The source of PEN's simulering-type values is:
 * https://github.com/navikt/pesys/blob/main/pen/domain/nav-domain-pensjon-pen-api/src/main/java/no/nav/domain/pensjon/kjerne/kodetabeller/SimuleringTypeCode.java
 */
enum class PenSimuleringType(val externalValue: String, val internalValue: SimuleringType) {

    ALDERSPENSJON("ALDER", SimuleringType.ALDERSPENSJON),
    ALDERSPENSJON_MED_AFP_PRIVAT("ALDER_M_AFP_PRIVAT", SimuleringType.ALDERSPENSJON_MED_AFP_PRIVAT);

    companion object {
        private val values = entries.toTypedArray()

        fun fromInternalValue(value: SimuleringType) =
            values.singleOrNull { it.internalValue == value } ?: ALDERSPENSJON
    }
}
