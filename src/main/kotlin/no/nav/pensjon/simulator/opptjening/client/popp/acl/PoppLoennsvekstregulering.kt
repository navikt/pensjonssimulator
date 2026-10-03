package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.util.*

data class PoppLoennsvekstregulering(
    val reguleringsbelop: Double? = null,
    val reguleringsDato: Date? = null
) {
    fun loennsvekstinformasjon() =
        LonnsvekstInformasjon().apply {
            reguleringsDatoLd = reguleringsDato?.toNorwegianLocalDate()
        }

    fun reguleringsinformasjon() =
        ReguleringsInformasjon().also {
            it.reguleringsbelop = reguleringsbelop ?: 0.0
        }
}