package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

// SimuleringsresultatAlderspensjon1963Plus
@JsonInclude(NON_NULL)
data class NavSimuleringResultV3(
    val alderspensjon: List<SimulertAlderspensjonV3>,
    val afpPrivat: List<SimulertPrivatAfpV3>,
    val afpOffentliglivsvarig: List<SimulertLivsvarigOffentligAfpV3>,
    val vilkaarsproeving: VilkaarsproevingResultatV3,
    val harNokTrygdetidForGarantipensjon: Boolean?,
    val trygdetid: Int,
    val opptjeningGrunnlagListe: List<SimulatorOpptjeningGrunnlagV3>
)

// no.nav.pensjon.pen.domain.api.simulering.dto.SimulertAlderspensjon
@JsonInclude(NON_NULL)
data class SimulertAlderspensjonV3(
    val alder: Int,
    val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonBeholdningFoerUttak: Int?
)

data class SimulertPrivatAfpV3(
    val alder: Int,
    val beloep: Int
)

data class SimulertLivsvarigOffentligAfpV3(
    val alder: Int,
    val beloep: Int
)

// no.nav.pensjon.pen.domain.api.simulering.SimulatorOpptjeningGrunnlag
data class SimulatorOpptjeningGrunnlagV3(
    val aar: Int,
    val pensjonsgivendeInntekt: Int
)

@JsonInclude(NON_NULL)
data class VilkaarsproevingResultatV3(
    val vilkaarErOppfylt: Boolean,
    val alternativ: AlternativtResultatV3?
)

@JsonInclude(NON_NULL)
data class AlternativtResultatV3(
    val gradertUttaksalder: AlderV3?,
    val uttaksgrad: Int,
    val heltUttaksalder: AlderV3
)

data class AlderV3(
    val aar: Int,
    val maaneder: Int
)
