package no.nav.pensjon.simulator.krav.client.pen.acl

import java.util.*

/**
 * PEN: ForsteVirkningsdatoGrunnlag
 */
data class PenFoersteVirkningDato(
    val sakType: String?, // SakTypeEnum
    val kravlinjeTypeEnum: String?, // KravlinjeTypeEnum
    val virkningsdato: Date?,
    val annenPerson: PenPenPerson?,
    // Not mapped; included to avoid UnrecognizedPropertyException:
    val kravFremsattDato: Date? = null,
    val bruker: PenPenPerson? = null
)
