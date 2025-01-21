package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

// Corresponds to SimulatorPersonligSimuleringResult in pensjonskalulator-backend
// (and previously to SimuleringsresultatAlderspensjon1963Plus in PEN)
@JsonInclude(NON_NULL)
data class NavSimuleringResultV3(
    val alderspensjonListe: List<NavAlderspensjonV3>,
    val alderspensjonMaanedsbeloep: NavMaanedsbeloepV3?,
    val privatAfpListe: List<NavPrivatAfpV3>,
    val livsvarigOffentligAfpListe: List<NavLivsvarigOffentligAfpV3>,
    val vilkaarsproeving: NavVilkaarsproevingResultatV3,
    val tilstrekkeligTrygdetidForGarantipensjon: Boolean?,
    val trygdetid: Int,
    val opptjeningGrunnlagListe: List<NavOpptjeningGrunnlagV3>
)

// no.nav.pensjon.pen.domain.api.simulering.dto.SimulertAlderspensjon
@JsonInclude(NON_NULL)
data class NavAlderspensjonV3(
    val alderAar: Int,
    val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonBeholdningFoerUttak: Int?
)

@JsonInclude(NON_NULL)
data class NavMaanedsbeloepV3(
    val gradertUttakBeloep: Int?,
    val heltUttakBeloep: Int
)

data class NavPrivatAfpV3(
    val alderAar: Int,
    val beloep: Int
)

data class NavLivsvarigOffentligAfpV3(
    val alderAar: Int,
    val beloep: Int
)

@JsonInclude(NON_NULL)
data class NavVilkaarsproevingResultatV3(
    val vilkaarErOppfylt: Boolean,
    val alternativ: NavAlternativtResultatV3?
)

// PEN: no.nav.pensjon.pen.domain.api.simulering.SimulatorOpptjeningGrunnlag
data class NavOpptjeningGrunnlagV3(
    val aar: Int,
    val pensjonsgivendeInntektBeloep: Int
)

@JsonInclude(NON_NULL)
data class NavAlternativtResultatV3(
    val gradertUttakAlder: NavAlderV3?,
    val uttaksgrad: Int,
    val heltUttakAlder: NavAlderV3
)

data class NavAlderV3(
    val aar: Int,
    val maaneder: Int
)
