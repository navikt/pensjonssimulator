package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate

// no.nav.service.pensjon.simulering.abstractsimulerapfra201.FinnKnekkpunkterRequest
data class KnekkpunktSpec(
    val kravhode: Kravhode,
    val simulering: SimuleringSpec,
    val soekerVirkningFom: LocalDate,
    val avdoedVirkningFom: LocalDate?,
    val forrigeAlderspensjonBeregningResultatVirkningFom: LocalDate?,
    val sakId: Long?
)
