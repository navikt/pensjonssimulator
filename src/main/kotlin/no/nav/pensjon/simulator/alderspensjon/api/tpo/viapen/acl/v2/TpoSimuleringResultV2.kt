package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * This class is basically a subset of SimulatorOutput.
 * It maps 1-to-1 with SimuleringResultLegacyV2 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringResultV2(
    val alderspensjon: TpoAlderspensjonV2? = null
    // afpPrivat: V1, V3 only
    // sisteGyldigeOpptjeningsAr: V3 only
)

// SimulertAlderspensjonV2 in PEN
data class TpoAlderspensjonV2(
    val pensjonsperiodeListe: List<TpoPensjonPeriodeV2>? = emptyList(),
    val pensjonsbeholdningListe: List<TpoPensjonBeholdningPeriodeV2>? = emptyList() // V2, V3 only
    // uttaksgradListe & simulertBeregningsinformasjonListe: V3 only
)

// PensjonsperiodeV2 in PEN
data class TpoPensjonPeriodeV2(
    val alderAar: Int? = null,
    val aarligBeloep: Int? = null,
    val beregningInformasjonListe: List<TpoBeregningInformasjonV2> // V1, V2 only
)

// PensjonsbeholdningPeriodeV2 in PEN
data class TpoPensjonBeholdningPeriodeV2(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val datoFom: LocalDate? = null,

    val pensjonsbeholdning: Double? = null,
    val garantipensjonsbeholdning: Double? = null,
    val garantitilleggsbeholdning: Double? = null
    // garantipensjonsniva: V3 only
)

// SimulertBeregningsinformasjonV2 in PEN
data class TpoBeregningInformasjonV2(
    // Values mapped in PEN HentBeregningsinformasjonForTPCommand.createSimuleringsdata:
    val poengAarFoer1992: Int? = null,
    val poengAarEtter1991: Int? = null,
    val sluttpoengtall: Double? = null,
    val kapittel19AnvendtTrygdetid: Int? = null,
    val basisGrunnpensjon: Double? = null,
    val basisTilleggspensjon: Double? = null,
    val basisPensjonstillegg: Double? = null,
    val forholdstall: Double? = null,
    val skjermingstillegg: Int? = null,
    val ufoeregrad: Int? = null,
    val inntektspensjon: Int? = null,
    val garantipensjon: Int? = null,
    val garantitillegg: Int? = null,
    val delingstall: Double? = null,
    // Values mapped in PEN SimulerAlderspensjonResponseV2Converter.createPensjonsperiodeList:
    val startMaaned: Int? = null, // V1, V2 only
    val uttaksgrad: Double? = null
    // datoFom, delytelser, simuleringsdata: V3 only
)
