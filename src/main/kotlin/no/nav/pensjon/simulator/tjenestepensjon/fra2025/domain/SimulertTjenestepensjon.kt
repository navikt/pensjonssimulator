package no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain

open class SimulertTjenestepensjon(
    val tpLeverandoer: String,
    var ordningsListe: List<Ordning> = emptyList(),
    var utbetalingsperioder: List<Utbetalingsperiode> = emptyList(),
    var aarsakIngenUtbetaling: List<String> = emptyList(),
    val betingetTjenestepensjonErInkludert: Boolean,
    var serviceData: List<String> = emptyList(),
    var erSisteOrdning: Boolean = false,
){
    override fun toString(): String {
        return "(tpLeverandoer='$tpLeverandoer', ordningsListe=$ordningsListe, utbetalingsperioder=$utbetalingsperioder, aarsakIngenUtbetaling=$aarsakIngenUtbetaling, betingetTjenestepensjonErInkludert=$betingetTjenestepensjonErInkludert"
    }
}

data class Ordning(val tpNummer: String)

open class SimulertTjenestepensjonMedMaanedsUtbetalinger(
    val tpLeverandoer: String,
    val tpNummer: String,
    var ordningsListe: List<Ordning> = emptyList(),
    var utbetalingsperioder: List<Maanedsutbetaling> = emptyList(),
    var aarsakIngenUtbetaling: List<String> = emptyList(),
    val betingetTjenestepensjonErInkludert: Boolean = false,
    var serviceData: List<String> = emptyList(),
){
    override fun toString(): String {
        return "SimulertTjenestepensjonMedMaanedsUtbetalinger(tpLeverandoer='$tpLeverandoer', tpNummer='$tpNummer', ordningsListe=$ordningsListe, utbetalingsperioder=$utbetalingsperioder, aarsakIngenUtbetaling=$aarsakIngenUtbetaling, betingetTjenestepensjonErInkludert=$betingetTjenestepensjonErInkludert"
    }
}