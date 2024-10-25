package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

/**
 * This class is basically a subset of SimulatorOutput.
 * It maps 1-to-1 with SimuleringResultLegacyV1 in PEN
 */
data class TpoSimuleringResultV1(
    val ap: TpoAlderspensjonV1? = null,
    val afpPrivat: List<TpoPrivatAfpPeriodeV1>? = emptyList() // V1, V3 only
    // sisteGyldigeOpptjeningsAr: V3 only
)

// SimulertAlderspensjonV1 in PEN
data class TpoAlderspensjonV1(
    val pensjonsperiodeListe: List<TpoPensjonPeriodeV1>? = emptyList(),
    // pensjonsbeholdningListe: V2, V3 only
    // uttaksgradListe: V3 only
    // simulertBeregningsinformasjonListe: V3 only
)

// SimulertAfpPrivatperiodeV1 in PEN
data class TpoPrivatAfpPeriodeV1(
    val alder: Int? = null, // V1 only
    val belopArlig: Int? = null // V1 only
    // afpOpptjening: V3 only
)

// PensjonsperiodeV1 in PEN
data class TpoPensjonPeriodeV1(
    val alder: Int? = null,
    val belop: Int? = null,
    val simulertBeregningsinformasjonListe: List<TpoBeregningInformasjonV1>? = emptyList() // V1, V2 only
)

// SimulertBeregningsinformasjonV1 in PEN
data class TpoBeregningInformasjonV1(
    val startMnd: Int? = null, // V1, V2 only
    val uttaksgrad: Double? = null
    // datoFom, delytelser, simuleringsdata: V3 only
)
