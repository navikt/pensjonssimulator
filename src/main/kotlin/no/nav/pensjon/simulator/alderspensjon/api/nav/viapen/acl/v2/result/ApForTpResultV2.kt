package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import java.util.*

/**
 * DTO version 2 of result of 'AP for TP' (alderspensjon for tjenestepensjon-simulering).
 * Corresponds 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011ResultatTp in PEN
 */
data class ApForTpResultV2(
    val ap: ApForTpAlderspensjonV2? = null,
    val afpPrivat: List<ApForTpPrivatAfpPeriodeV2> = emptyList(),
    val afpOffentlig: ApForTpSimuleringResultatV2? = null,
    val sivilstand: SivilstandEnum? = null
)

data class ApForTpAlderspensjonV2(
    val pensjonsbeholdningListe: List<ApForTpBeholdningPeriodeV2>? = mutableListOf(),
    val simulertBeregningsinformasjonListe: List<ApForTpBeregningInformasjonV2>? = mutableListOf()
)

data class ApForTpBeholdningPeriodeV2(
    val datoFom: Date? = null,
    val pensjonsbeholdning: Double? = null,
    val garantipensjonsbeholdning: Double? = null,
    val garantitilleggsbeholdning: Double? = null
)

data class ApForTpBeregningInformasjonV2(
    val datoFom: Date? = null,
    val basisgp: Double? = null,
    val basistp: Double? = null,
    val basispt: Double? = null,
    val ufg: Int? = null,
    val forholdstall: Double? = null,
    val delingstall: Double? = null,
    val tt_anv_kap19: Int? = null,
    val pa_f92: Int? = null,
    val pa_e91: Int? = null,
    val spt: Double? = null
)

data class ApForTpPrivatAfpPeriodeV2(
    val afpOpptjening: Int? = null,
    val alder: Int? = null,
    val komptillegg: Int? = null
)

data class ApForTpSimuleringResultatV2(
    val beregning: ApForTpBeregningV2? = null // beregning.get(0)
)

data class ApForTpBeregningV2(
    val brutto: Int? = 0,
    val tilleggspensjonListe: List<ApForTpTilleggspensjonV2>? = emptyList()
)

data class ApForTpTilleggspensjonV2(
    val spt: ApForTpSluttpoengtallV2? = null
)

data class ApForTpSluttpoengtallV2(
    val poengrekke: ApForTpPoengrekkeV2 // poengrekkeList.get(0)
)

data class ApForTpPoengrekkeV2(
    val tpi: Int? = 0 // tidligere pensjonsgivende inntekt
)
