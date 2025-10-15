package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

/**
 * Version 3 of the data transfer object representing a result of the 'simuler alderspensjon & privat AFP' service.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonResponseV3
 *   + AFP part of no.nav.tjeneste.ekstern.simulerepensjon.v1.meldinger.HentSimulertPensjonResponse
 */
data class AlderspensjonOgPrivatAfpResultV3(
    val alderspensjonsperioder: List<ApOgPrivatAfpAlderspensjonsperiodeResultV3>,
    val privatAfpPerioder: List<ApOgPrivatAfpPrivatAfpPeriodeResultV3>, // from HentSimulertPensjonResponse
    val harNaavaerendeUttak: Boolean,
    val harTidligereUttak: Boolean
)

/**
 * Version 3 of 'alderspensjonsperiode' in result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Pensjonsperiode
 *    + no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.SimulertFleksibelAlderspensjonPeriode
 *    + no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.UttaksgradPeriode
 */
data class ApOgPrivatAfpAlderspensjonsperiodeResultV3(
    val alder: Int, // from SimulertFleksibelAlderspensjonPeriode
    val beloep: Int, // from Pensjonsperiode, corresponds to 'beloep' in SimulertFleksibelAlderspensjonPeriode
    val datoFom: String, // from Pensjonsperiode
    // NB: In V3 for TPO this is not a list:
    val uttaksperiode: List<ApOgPrivatAfpUttaksperiodeResultV3> // from SimulertFleksibelAlderspensjonPeriode
)

/**
 * Version 3 of 'privat AFP-periode' in result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.SimulertAfpPrivatPeriode
 */
data class ApOgPrivatAfpPrivatAfpPeriodeResultV3(
    val alder: Int, // antall år
    val beloep: Int
)

/**
 * Version 3 of 'uttaksperiode' in result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.UttaksgradPeriode
 */
data class ApOgPrivatAfpUttaksperiodeResultV3(
    val startmaaned: Int,
    val uttaksgrad: Int
)
