package no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain

import no.nav.pensjon.simulator.tech.security.egress.config.EgressService

//TODO use data class?
open class SimulertTjenestepensjon(
    val tpLeverandoer: String, //TODO use EgressService?
    val ordningsListe: List<Ordning> = emptyList(),
    val utbetalingsperioder: List<Utbetalingsperiode> = emptyList(),
    val aarsakIngenUtbetaling: List<String> = emptyList(),
    val betingetTjenestepensjonErInkludert: Boolean,
    val serviceData: List<String> = emptyList(),
    val erSisteOrdning: Boolean = false
) {
    override fun toString(): String =
        "(tpLeverandoer='$tpLeverandoer'," +
                " ordningsListe=$ordningsListe," +
                " utbetalingsperioder=$utbetalingsperioder," +
                " aarsakIngenUtbetaling=$aarsakIngenUtbetaling," +
                " betingetTjenestepensjonErInkludert=$betingetTjenestepensjonErInkludert"
}

open class SimulertTjenestepensjonMedMaanedsUtbetalinger(
    val tpLeverandoer: EgressService,
    val tpNummer: String,
    val ordningsListe: List<Ordning> = emptyList(),
    val utbetalingsperioder: List<Maanedsutbetaling> = emptyList(),
    val aarsakIngenUtbetaling: List<String> = emptyList(),
    val betingetTjenestepensjonErInkludert: Boolean = false,
    val serviceData: List<String> = emptyList()
) {
    override fun toString(): String =
        "SimulertTjenestepensjonMedMaanedsUtbetalinger(tpLeverandoer='$tpLeverandoer'," +
                " tpNummer='$tpNummer'," +
                " ordningsListe=$ordningsListe," +
                " utbetalingsperioder=$utbetalingsperioder," +
                " aarsakIngenUtbetaling=$aarsakIngenUtbetaling," +
                " betingetTjenestepensjonErInkludert=$betingetTjenestepensjonErInkludert"
}

data class Ordning(
    val tpNummer: String
)