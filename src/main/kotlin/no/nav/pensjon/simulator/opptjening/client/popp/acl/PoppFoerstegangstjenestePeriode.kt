package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.ForstegangstjenestetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForstegangstjenestePeriode
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.util.*

data class PoppFoerstegangstjenestePeriode(
    val periodeType: String? = null, // ForstegangstjenestetypeEnum
    val fomDato: Date? = null,
    val tomDato: Date? = null
) {
    fun toGrunnlag() =
        ForstegangstjenestePeriode().apply {
            periodeTypeEnum = ForstegangstjenestetypeEnum.fromValue(periodeType)
            fomDatoLd = fomDato?.toNorwegianLocalDate()
            tomDatoLd = tomDato?.toNorwegianLocalDate()
        }
}