package no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3out

import java.time.LocalDate

/**
 * This class is basically a subset of SimulatorOutput.
 * It maps 1-to-1 with SimuleringResultLegacyV3 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringResultV3(
    val ap: SimulertAlderspensjonV3? = null,
    val afpPrivat: List<SimulertAfpPrivatperiodeV3> = emptyList(),
    val sisteGyldigeOpptjeningsAr: Int? = null
)

data class SimulertAlderspensjonV3(
    val pensjonsperiodeListe: List<PensjonsperiodeV3> = emptyList(),
    val pensjonsbeholdningListe: List<PensjonsbeholdningPeriodeV3> = emptyList(),
    val uttaksgradListe: List<UttaksgradV3> = emptyList(),
    val simulertBeregningsinformasjonListe: List<SimulertBeregningsinformasjonV3> = emptyList()
)

data class SimulertAfpPrivatperiodeV3(
    val afpOpptjening: Int? = null
)

data class PensjonsperiodeV3(
    val alder: Int? = null,
    val belop: Int? = null
)

data class PensjonsbeholdningPeriodeV3(
    val datoFom: LocalDate? = null,
    val garantipensjonsbeholdning: Double? = null,
    val garantitilleggsbeholdning: Double? = null,
    val pensjonsbeholdning: Double? = null,
    val garantipensjonsniva: GarantipensjonsnivaV3? = null
)

data class GarantipensjonsnivaV3(
    val belop: Double? = null,
    val satsType: String? = null,
    val sats: Double? = null,
    val tt_anv: Int? = null
)

data class UttaksgradV3(
    val uttaksgrad: Int? = null,
    val fomDato: LocalDate? = null,
    val tomDato: LocalDate? = null
)

data class SimulertBeregningsinformasjonV3(
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
