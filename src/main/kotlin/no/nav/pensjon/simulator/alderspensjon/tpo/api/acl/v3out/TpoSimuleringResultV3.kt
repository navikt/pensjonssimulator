package no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3out

import java.time.LocalDate

/**
 * This class is basically a subset of SimulatorOutput.
 * It maps 1-to-1 with SimuleringResultLegacyV3 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringResultV3(
    val ap: TpoAlderspensjonV3? = null,
    val afpPrivat: List<TpoPrivatAfpPeriodeV3> = emptyList(),
    val sisteGyldigeOpptjeningsAr: Int? = null
)

// SimulertAlderspensjonV3 in PEN
data class TpoAlderspensjonV3(
    val pensjonsperiodeListe: List<TpoPensjonPeriodeV3> = emptyList(),
    val pensjonsbeholdningListe: List<TpoPensjonBeholdningPeriodeV3> = emptyList(),
    val uttaksgradListe: List<TpoUttakGradV3> = emptyList(),
    val simulertBeregningsinformasjonListe: List<TpoBeregningInformasjonV3> = emptyList()
)

// SimulertAfpPrivatperiodeV3 in PEN
data class TpoPrivatAfpPeriodeV3(
    val afpOpptjening: Int? = null
)

// PensjonsperiodeV3 in PEN
data class TpoPensjonPeriodeV3(
    val alder: Int? = null,
    val belop: Int? = null
)

// PensjonsbeholdningPeriodeV3 in PEN
data class TpoPensjonBeholdningPeriodeV3(
    val datoFom: LocalDate? = null,
    val garantipensjonsbeholdning: Double? = null,
    val garantitilleggsbeholdning: Double? = null,
    val pensjonsbeholdning: Double? = null,
    val garantipensjonsniva: TpoGarantipensjonNivaaV3? = null
)

// GarantipensjonsnivaV3 in PEN
data class TpoGarantipensjonNivaaV3(
    val belop: Double? = null,
    val satsType: String? = null,
    val sats: Double? = null,
    val tt_anv: Int? = null
)

// UttaksgradV3 in PEN
data class TpoUttakGradV3(
    val uttaksgrad: Int? = null,
    val fomDato: LocalDate? = null,
    val tomDato: LocalDate? = null
)

// SimulertBeregningsinformasjonV3 in PEN
data class TpoBeregningInformasjonV3(
    val datoFom: LocalDate? = null,
    val uttaksgrad: Double? = null,

    // Delytelser:
    val gp: Int? = null,
    val tp: Int? = null,
    val pt: Int? = null,
    val minstenivaTilleggIndividuelt: Int? = null,
    val inntektspensjon: Int? = null,
    val garantipensjon: Int? = null,
    val garantitillegg: Int? = null,
    val skjermt: Int? = null,

    // Simuleringsdata:
    val pa_f92: Int? = null,
    val pa_e91: Int? = null,
    val spt: Int? = null,
    val tt_anv_kap19: Int? = null,
    val basisgp: Int? = null,
    val basistp: Int? = null,
    val basispt: Int? = null,
    val forholdstall: Int? = null,
    val delingstall: Int? = null,
    val ufg: Int? = null
)
