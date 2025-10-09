package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import kotlin.String

/**
 * Version 3 of result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonResponseV3
 */
data class AlderspensjonResultV3(
    val pensjonsperioder: List<PensjonsperiodeResultV3>?,
    val simuleringsdataListe: List<SimuleringsdataResultV3>?,
    val pensjonsbeholdningsperioder: List<PensjonsbeholdningPeriodeResultV3>?,
    val alderspensjonFraFolketrygden: List<AlderspensjonFraFolketrygdenResultV3>?,
    val harUttak: Boolean,
    val harTidligereUttak: Boolean,
    val afpPrivatBeholdningVedUttak: Int?,
    val sisteGyldigeOpptjeningsAr: Int?
)

/**
 * Version 3 of 'alderspensjon fra folketrygden' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.AlderspensjonFraFolketrygden
 */
data class AlderspensjonFraFolketrygdenResultV3(
    val datoFom: String?,
    val delytelser: List<DelytelseResultV3>?,
    val uttaksgrad: Int?
)

/**
 * Version 3 of 'delytelse' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Delytelse
 */
data class DelytelseResultV3(
    val pensjonstype: String?,
    val belop: Int?
)

/**
 * Version 3 of 'pensjonsperiode' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Pensjonsperiode
 */
data class PensjonsperiodeResultV3(
    val arligUtbetaling: Int?,
    val datoFom: String?
)

/**
 * Version 3 of 'pensjonsbeholdningsperiode' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.PensjonsbeholdningPeriode
 */
data class PensjonsbeholdningPeriodeResultV3(
    val pensjonsbeholdning: Double?,
    val garantipensjonsbeholdning: Double?,
    val garantitilleggsbeholdning: Double?,
    val datoFom: String?,
    val garantipensjonsniva: GarantipensjonsnivaaResultV3?
)

/**
 * Version 3 of 'simuleringsdata' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Simuleringsdata
 */
data class SimuleringsdataResultV3(
    val poengArTom1991: Int?,
    val poengArFom1992: Int?,
    val sluttpoengtall: Double?,
    val anvendtTrygdetid: Int?,
    val basisgp: Double?,
    val basistp: Double?,
    val basispt: Double?,
    val forholdstallUttak: Double?,
    val delingstallUttak: Double?,
    val uforegradVedOmregning: Int?,
    val datoFom: String?
)

/**
 * Version 3 of 'garantipensjonsnivå' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Garantipensjonsniva
 */
data class GarantipensjonsnivaaResultV3(
    val belop: Double?,
    val satsType: String?,
    val sats: Double?,
    val tt_anv: Int?
)
