package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Ref. API specification: https://confluence.adeo.no/x/hJRHK
 */
data class AfpEtterfulgtAvAlderspensjonResultV0(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<AarsakIkkeSuccessV0>,
    val folketrygdberegnetAfp: FolketrygdberegnetAfpV0?,
    val alderspensjonFraFolketrygden: List<AlderspensjonFraFolketrygdenV0>,
)

data class FolketrygdberegnetAfpV0(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fraOgMedDato: LocalDate,
    val tidligereInntekt: Int?, //
    val afpGrad: Int,
    val grunnpensjon: GrunnpensjonV0,
    val tilleggspensjon: TilleggspensjonV0?,
    val saertillegg: SaertilleggV0?,
    val maanedligAfpTillegg: Int,
    val sumMaanedligUtbetaling: Int,
)

data class GrunnpensjonV0(
    val maanedligUtbetaling: Int,
    val grunnbeloep: Int?, //Foreløpig tomt
    val grunnpensjonsats: Double?, //Foreløpig tomt
    val trygdetid: Int,
)

data class TilleggspensjonV0(
    val maanedligUtbetaling: Int,
    val grunnbeloep: Int?, //Foreløpig tomt
    val sluttpoengTall: Double,
    val antallPoengaarTilOgMed1991: Int,
    val antallPoengaarFraOgMed1992: Int,
)

data class SaertilleggV0(
    val maanedligUtbetaling: Int,
    val saertilleggsats: Double?, //Foreløpig tomt
)

data class AlderspensjonFraFolketrygdenV0(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fraOgMedDato: LocalDate?, //nullable for test
    val andelKapittel19: Double,
    val alderspensjonKapittel19: AlderspensjonKapittel19V0,
    val andelKapittel20: Double,
    val alderspensjonKapittel20: AlderspensjonKapittel20V0,
    val sumMaanedligUtbetaling: Int,
)

data class AlderspensjonKapittel19V0(
    val grunnpensjon: GrunnpensjonV0,
    val tilleggspensjon: TilleggspensjonV0,
    val pensjonstillegg: PensjonstilleggV0,
    val forholdstall: Double,
)

data class PensjonstilleggV0(
    val maanedligUtbetaling: Int,
    val minstepensjonsnivaasats: Int?, //Foreløpig tomt
)

data class AlderspensjonKapittel20V0(
    val inntektspensjon: InntektspensjonV0,
    val garantipensjon: GarantipensjonV0,
    val delingstall: Double,
)

data class GarantipensjonV0(
    val maanedligUtbetaling: Int,
    val garantipensjonssats: Int?, //Foreløpig tomt
    val trygdetid: Int,
)

data class InntektspensjonV0(
    val maanedligUtbetaling: Int,
    val pensjonsbeholdningFoerUttak: Int,
)

data class AarsakIkkeSuccessV0(
    val statusKode: String,
    val statusBeskrivelse: String
)
