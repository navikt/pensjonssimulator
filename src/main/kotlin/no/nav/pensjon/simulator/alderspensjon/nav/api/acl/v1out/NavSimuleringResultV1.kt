package no.nav.pensjon.simulator.alderspensjon.nav.api.acl.v1out

// SimuleringsresultatAlderspensjon1963Plus
data class NavSimuleringResultV1(
    val alderspensjon: List<SimulertAlderspensjonV1>,
    val afpPrivat: List<SimulertPrivatAfpV1>,
    val afpOffentliglivsvarig: List<SimulertLivsvarigOffentligAfpV1>,
    val vilkaarsproeving: VilkaarsproevingResultatV1,
    val harNokTrygdetidForGarantipensjon: Boolean?,
    val trygdetid: Int,
    val opptjeningGrunnlagListe: List<SimulatorOpptjeningGrunnlagV1>
)

// no.nav.pensjon.pen.domain.api.simulering.dto.SimulertAlderspensjon
data class SimulertAlderspensjonV1(
    val alder: Int,
    val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonBeholdningFoerUttak: Int?
)

data class SimulertPrivatAfpV1(
    val alder: Int,
    val beloep: Int
)

data class SimulertLivsvarigOffentligAfpV1(
    val alder: Int,
    val beloep: Int
)

// no.nav.pensjon.pen.domain.api.simulering.SimulatorOpptjeningGrunnlag
data class SimulatorOpptjeningGrunnlagV1(
    val aar: Int,
    val pensjonsgivendeInntekt: Int
)

data class VilkaarsproevingResultatV1(
    val vilkaarErOppfylt: Boolean,
    val alternativ: AlternativtResultatV1?
)

data class AlternativtResultatV1(
    val gradertUttaksalder: AlderV1?,
    val uttaksgrad: Int,
    val heltUttaksalder: AlderV1
)

data class AlderV1(
    val aar: Int,
    val maaneder: Int
)
