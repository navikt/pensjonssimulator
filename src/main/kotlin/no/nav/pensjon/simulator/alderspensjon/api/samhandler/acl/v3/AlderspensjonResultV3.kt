package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import kotlin.String

/**
 * Version 3 of result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonResponseV3
 */
data class AlderspensjonResultV3(
    val pensjonsperioder: List<PensjonsperiodeResultV3>? = null,
    val simuleringsdataListe: List<SimuleringsdataResultV3>? = null,
    val pensjonsbeholdningsperioder: List<PensjonsbeholdningPeriodeResultV3>? = null,
    val alderspensjonFraFolketrygden: List<AlderspensjonFraFolketrygdenResultV3>? = null,
    val harUttak: Boolean = false,
    val harTidligereUttak: Boolean = false,
    val afpPrivatBeholdningVedUttak: Int? = null,
    val sisteGyldigeOpptjeningsAr: Int? = null
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

data class DelytelseResultV3T(
    val pensjonstype: String?,
    val belop: Int?
) {
    //override fun from(pensjonstype: String?, belop: Int?) = DelytelseResultV3T(pensjonstype, belop)
}

interface DelytelseResultV3I {
    //fun from(pensjonstype: String?, belop: Int?): DelytelseResultV3I
}

/**
 * Version 3 of 'pensjonsperiode' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Pensjonsperiode
 */
data class PensjonsperiodeResultV3(
    val arligUtbetaling: Int? = null,
    val datoFom: String? = null
)

/**
 * Version 3 of 'pensjonsbeholdningsperiode' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.PensjonsbeholdningPeriode
 */
data class PensjonsbeholdningPeriodeResultV3(
    val pensjonsbeholdning: Double? = null,
    val garantipensjonsbeholdning: Double? = null,
    val garantitilleggsbeholdning: Double? = null,
    val datoFom: String? = null,
    val garantipensjonsniva: GarantipensjonsnivaaResultV3? = null
)

/**
 * Version 3 of 'simuleringsdata' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Simuleringsdata
 */
data class SimuleringsdataResultV3(
    val poengArTom1991: Int? = null,
    val poengArFom1992: Int? = null,
    val sluttpoengtall: Double? = null,
    val anvendtTrygdetid: Int? = null,
    val basisgp: Double? = null,
    val basistp: Double? = null,
    val basispt: Double? = null,
    val forholdstallUttak: Double? = null,
    val delingstallUttak: Double? = null,
    val uforegradVedOmregning: Int? = null,
    val datoFom: String? = null
)

/**
 * Version 3 of 'garantipensjonsnivå' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Garantipensjonsniva
 */
data class GarantipensjonsnivaaResultV3(
    val belop: Double? = null,
    val satsType: String? = null,
    val sats: Double? = null,
    val tt_anv: Int? = null
)
