package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.ForstegangstjenestetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForstegangstjenestePeriode
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.util.*

data class PoppFoerstegangstjenestePeriode(
    val periodeType: String? = null,
    val fomDato: Date? = null,
    val tomDato: Date? = null
) {
    fun toGrunnlag() =
        ForstegangstjenestePeriode().apply {
            periodeTypeEnum = periodeType?.let { enumValueOf<ForstegangstjenestetypeEnum>(it) }
            fomDatoLd = fomDato?.toNorwegianLocalDate()
            tomDatoLd = tomDato?.toNorwegianLocalDate()
        }
}