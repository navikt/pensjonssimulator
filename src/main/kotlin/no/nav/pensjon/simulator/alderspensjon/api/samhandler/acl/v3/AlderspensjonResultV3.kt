package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertDelytelse
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.LocalDateUtil.foersteDagMaanedenEtterBursdag
import java.time.LocalDate
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
    val harUttak: Boolean,
    val harTidligereUttak: Boolean,
    val afpPrivatBeholdningVedUttak: Int? = null,
    val sisteGyldigeOpptjeningsAr: Int? = null
)

/**
 * Version 3 of 'alderspensjon fra folketrygden' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.AlderspensjonFraFolketrygden
 */
data class AlderspensjonFraFolketrygdenResultV3(
    val datoFom: String? = null,
    val delytelser: List<DelytelseResultV3>? = null,
    val uttaksgrad: Int? = null
) {
    companion object {
        fun transferable(source: SimulertBeregningInformasjon) =
            AlderspensjonFraFolketrygdenResultV3(
                datoFom = source.datoFom?.toString(),
                delytelser = SimulatorOutputConverter.delytelser(source).map(DelytelseResultV3::transferable),
                uttaksgrad = source.uttakGrad?.toInt()
            )
    }
}

/**
 * Version 3 of 'delytelse' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Delytelse
 */
data class DelytelseResultV3(
    val pensjonstype: String? = null,
    val belop: Int? = null
) {
    companion object {
        fun transferable(source: SimulertDelytelse) =
            DelytelseResultV3(
                pensjonstype = source.type.name,
                belop = source.beloep
            )
    }
}

/**
 * Version 3 of 'pensjonsperiode' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Pensjonsperiode
 */
data class PensjonsperiodeResultV3(
    val arligUtbetaling: Int? = null,
    val datoFom: String? = null
) {
    companion object {
        fun transferable(source: PensjonPeriode, foedselsdato: LocalDate) =
            PensjonsperiodeResultV3(
                arligUtbetaling = source.beloep,
                datoFom = source.alderAar?.let {
                    foersteDagMaanedenEtterBursdag(foedselsdato, alderAar = it).toString()
                }
            )
    }
}

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
) {
    companion object {
        fun transferable(source: BeholdningPeriode) =
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = source.pensjonsbeholdning,
                garantipensjonsbeholdning = source.garantipensjonsbeholdning,
                garantitilleggsbeholdning = source.garantitilleggsbeholdning,
                datoFom = source.datoFom.toString(),
                garantipensjonsniva = source.garantipensjonsniva?.let(GarantipensjonsnivaaResultV3::transferable)
            )
    }
}

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
) {
    companion object {
        fun transferable(source: SimulertBeregningInformasjon) =
            SimuleringsdataResultV3(
                poengArTom1991 = source.pa_f92,
                poengArFom1992 = source.pa_e91,
                sluttpoengtall = source.spt,
                anvendtTrygdetid = source.tt_anv_kap19,
                basisgp = source.basisGrunnpensjon,
                basistp = source.basisTilleggspensjon,
                basispt = source.basisPensjonstillegg,
                forholdstallUttak = source.forholdstall,
                delingstallUttak = source.delingstall,
                uforegradVedOmregning = source.ufoereGrad,
                datoFom = source.datoFom?.toString()
            )
    }
}

/**
 * Version 3 of 'garantipensjonsnivå' in result of 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Garantipensjonsniva
 */
data class GarantipensjonsnivaaResultV3(
    val belop: Double? = null,
    val satsType: String? = null,
    val sats: Double? = null,
    val tt_anv: Int? = null
) {
    companion object {
        fun transferable(source: GarantipensjonNivaa) =
            GarantipensjonsnivaaResultV3(
                belop = source.beloep,
                satsType = source.satsType,
                sats = source.sats,
                tt_anv = source.anvendtTrygdetid
            )
    }
}