package no.nav.pensjon.simulator.krav.client.pen.acl

import java.util.*

data class PenFoersteVirkningDato(
    val sakType: String?, // SakType
    val kravlinjeType: String?, // KravlinjeTypeEnum
    val virkningsdato: Date?,
    val annenPerson: PenPenPerson?
)
