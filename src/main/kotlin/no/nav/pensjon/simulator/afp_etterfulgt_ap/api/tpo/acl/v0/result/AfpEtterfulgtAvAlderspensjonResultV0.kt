package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

/**
 * Ref. API specification: https://confluence.adeo.no/x/hJRHK
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
data class AfpEtterfulgtAvAlderspensjonResultV0(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<AarsakIkkeSuccessV0>,
    val folketrygdberegnetAfp: FolketrygdberegnetAfpV0?,
    val alderspensjonFraFolketrygden: List<AlderspensjonFraFolketrygdenV0>,
)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class FolketrygdberegnetAfpV0(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fraOgMedDato: LocalDate,
    val beregnetTidligereInntekt: Int?,
    val fremtidigAarligInntektTilAfpUttak: Int,
    val afpGrad: Int,
    val afpAvkortetTil70Prosent: Boolean,
    val grunnpensjon: GrunnpensjonV0,
    val tilleggspensjon: TilleggspensjonV0?,
    val saertillegg: SaertilleggV0?,
    val maanedligAfpTillegg: Int,
    val sumMaanedligUtbetaling: Int,
)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class GrunnpensjonV0(
    val maanedligUtbetaling: Int,
    val grunnbeloep: Int,
    val grunnpensjonsats: Double?,
    val trygdetid: Int,
)

data class TilleggspensjonV0(
    val maanedligUtbetaling: Int,
    val grunnbeloep: Int,
    val sluttpoengTall: Double,
    val antallPoengaarTilOgMed1991: Int,
    val antallPoengaarFraOgMed1992: Int,
)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class SaertilleggV0(
    val maanedligUtbetaling: Int,
    val saertilleggsats: Double?,
)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class AlderspensjonFraFolketrygdenV0(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fraOgMedDato: LocalDate,
    val andelKapittel19: Double?,
    val alderspensjonKapittel19: AlderspensjonKapittel19V0?,
    val andelKapittel20: Double?,
    val alderspensjonKapittel20: AlderspensjonKapittel20V0?,
    val sumMaanedligUtbetaling: Int,
)

data class AlderspensjonKapittel19V0(
    val grunnpensjon: GrunnpensjonV0,
    val tilleggspensjon: TilleggspensjonV0,
    val pensjonstillegg: PensjonstilleggV0,
    val forholdstall: Double,
)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class PensjonstilleggV0(
    val maanedligUtbetaling: Int,
    val minstepensjonsnivaaSats: Double?,
)

data class AlderspensjonKapittel20V0(
    val inntektspensjon: InntektspensjonV0,
    val garantipensjon: GarantipensjonV0,
    val delingstall: Double,
)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class GarantipensjonV0(
    val maanedligUtbetaling: Int,
    val garantipensjonssats: Double?,
    val trygdetid: Int,
)

data class InntektspensjonV0(
    val maanedligUtbetaling: Int,
    val pensjonsbeholdningFoerUttak: Int,
)

data class AarsakIkkeSuccessV0(
    val statusKode: String,
    val statusBeskrivelse: String
) {
    companion object {
        val FEIL_I_GRUNNLAG = AarsakIkkeSuccessV0("FEIL_I_GRUNNLAG", "Beregningen kunne ikke fullføres på grunn av inkonsistens i datagrunnlaget.")
        val FOR_HOEY_ALDER = AarsakIkkeSuccessV0("FOR_HOEY_ALDER", "Brukerens alder overstiger tillatt grense.")
        val FOR_LAV_ALDER = AarsakIkkeSuccessV0("FOR_LAV_ALDER", "Brukerens alder er under nedre aldersgrense.")
        val AFP_ER_AVSLAATT = AarsakIkkeSuccessV0("AFP_ER_AVSLAATT", "AFP er avslått")
        val UTILSTREKKELIG_OPPTJENING = AarsakIkkeSuccessV0("UTILSTREKKELIG_OPPTJENING", "Utilstrekkelig opptjening")
        val UTILSTREKKELIG_TRYGDETID = AarsakIkkeSuccessV0("UTILSTREKKELIG_TRYGDETID", "Utilstrekkelig trygdetid")
    }
}
