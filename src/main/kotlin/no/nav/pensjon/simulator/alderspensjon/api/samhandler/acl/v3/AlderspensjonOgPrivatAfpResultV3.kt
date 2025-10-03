package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

/**
 * Version 3 of result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonResponseV3
 *   + AFP part of no.nav.tjeneste.ekstern.simulerepensjon.v1.meldinger.HentSimulertPensjonResponse
 */
data class AlderspensjonOgPrivatAfpResultV3(
    val alderspensjonsperioder: List<AlderspensjonsperiodeResultV3>,
    val privatAfpPerioder: List<PrivatAfpPeriodeResultV3>, // from HentSimulertPensjonResponse
    val harUttak: Boolean,
    val harTidligereUttak: Boolean
)


/**
 * Version 3 of 'alderspensjonsperiode' in result of 'simuler alderspensjon & privat AFP'.
 * Extended variant of PensjonsperiodeResultV3.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Pensjonsperiode
 *    + no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.SimulertFleksibelAlderspensjonPeriode
 *    + no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.UttaksgradPeriode
 */
data class AlderspensjonsperiodeResultV3(
    val arligUtbetaling: Int, // from Pensjonsperiode, corresponds to 'beloep' in SimulertFleksibelAlderspensjonPeriode
    val datoFom: String, // from Pensjonsperiode
    val alder: Int, // from SimulertFleksibelAlderspensjonPeriode
    // NB: In V3 for TPO this is not a list:
    val uttaksgradPeriode: List<UttaksperiodeResultV3> // from SimulertFleksibelAlderspensjonPeriode
)

/**
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.UttaksgradPeriode
 */
data class UttaksperiodeResultV3(
    val startmaned: Int,
    val uttaksgrad: Int
)

/**
 * Version 3 of 'privat AFP-periode' in result of 'simuler alderspensjon & privat AFP'.
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.SimulertAfpPrivatPeriode
 */
data class PrivatAfpPeriodeResultV3(
    val belop: Int, // 'belop' (not 'beloep'), since DelytelseResultV3 uses 'belop'
    val alder: Int
)
