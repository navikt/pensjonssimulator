package no.nav.pensjon.simulator.afp.offentlig.pre2025.api.acl.v0.result

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS
import java.time.LocalDate

/**
 * Ref. API specification: https://confluence.adeo.no/x/hJRHK
 */
@JsonInclude(ALWAYS)
data class AfpEtterfulgtAvAlderspensjonResultV0(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<AarsakIkkeSuccessV0>,
    val folketrygdberegnetAfp: FolketrygdberegnetAfpV0?,
    val alderspensjonFraFolketrygden: List<AlderspensjonFraFolketrygdenV0>,
)

@JsonInclude(ALWAYS)
data class FolketrygdberegnetAfpV0(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val beregnetTidligereInntekt: Int?,
    val sisteLignetInntektBrukt: Boolean,
    val sisteLignetInntektAar: Int?,
    val afpGrad: Int,
    val afpAvkortetTil70Prosent: Boolean,
    val grunnpensjon: GrunnpensjonV0,
    val tilleggspensjon: TilleggspensjonV0?,
    val saertillegg: SaertilleggV0?,
    val maanedligAfpTillegg: Int,
    val sumMaanedligUtbetaling: Int,
)

@JsonInclude(ALWAYS)
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

@JsonInclude(ALWAYS)
data class SaertilleggV0(
    val maanedligUtbetaling: Int,
)

@JsonInclude(ALWAYS)
data class AlderspensjonFraFolketrygdenV0(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
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
)

@JsonInclude(ALWAYS)
data class PensjonstilleggV0(
    val maanedligUtbetaling: Int,
    val minstepensjonsnivaaSats: Double?,
)

data class AlderspensjonKapittel20V0(
    val inntektspensjon: InntektspensjonV0,
    val garantipensjon: GarantipensjonV0,
)

@JsonInclude(ALWAYS)
data class GarantipensjonV0(
    val maanedligUtbetaling: Int,
    val garantipensjonsbeholdningForUttak: Int?,
    val trygdetid: Int,
)

data class InntektspensjonV0(
    val maanedligUtbetaling: Int,
    val pensjonsbeholdningForUttak: Int,
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
