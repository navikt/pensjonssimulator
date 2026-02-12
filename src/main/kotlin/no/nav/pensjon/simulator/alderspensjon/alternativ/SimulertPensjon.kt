package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import no.nav.pensjon.simulator.trygdetid.Trygdetid
import java.time.LocalDate

data class SimulertPensjon(
    val alderspensjon: List<SimulertAarligAlderspensjon>,
    val alderspensjonFraFolketrygden: List<SimulertAlderspensjonFraFolketrygden>,
    val privatAfp: List<SimulertPrivatAfp>,
    val pre2025OffentligAfp: SimulertPre2025OffentligAfp?,
    val livsvarigOffentligAfp: List<SimulertLivsvarigOffentligAfp>,
    val pensjonBeholdningPeriodeListe: List<SimulertPensjonBeholdningPeriode>,
    val harUttak: Boolean,
    val primaerTrygdetid: Trygdetid,
    val opptjeningGrunnlagListe: List<OpptjeningGrunnlag>
)

data class SimulertAarligAlderspensjon(
    val alderAar: Int,
    val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonBeholdningFoerUttak: Int?,
    val andelsbroekKap19: Double?,
    val andelsbroekKap20: Double?,
    val sluttpoengtall: Double?,
    val trygdetidKap19: Int?,
    val trygdetidKap20: Int?,
    val poengaarFoer92: Int?,
    val poengaarEtter91: Int?,
    val forholdstall: Double?,
    val grunnpensjon: Int?,
    val tilleggspensjon: Int?,
    val pensjonstillegg: Int?,
    val skjermingstillegg: Int?,
    val kapittel19Gjenlevendetillegg: Int?
)

data class SimulertAlderspensjonFraFolketrygden(
    val datoFom: LocalDate,
    val delytelseListe: List<SimulertDelytelse>,
    val uttakGrad: Int,
    val maanedligBeloep: Int
)

data class SimulertPrivatAfp(
    val alderAar: Int,
    val beloep: Int,
    val kompensasjonstillegg: Int,
    val kronetillegg: Int,
    val livsvarig: Int,
    val maanedligBeloep: Int
)

data class SimulertPre2025OffentligAfp(
    val alderAar: Int,
    val totaltAfpBeloep: Int,
    val tidligereArbeidsinntekt: Int,
    val grunnbeloep: Int,
    val sluttpoengtall: Double,
    val trygdetid: Int,
    val poengaarTom1991: Int,
    val poengaarFom1992: Int,
    val grunnpensjon: Int,
    val tilleggspensjon: Int,
    val afpTillegg: Int,
    val saertillegg: Int,
    val afpGrad: Int,
    val afpAvkortetTil70Prosent: Boolean
)

data class SimulertLivsvarigOffentligAfp(
    val alderAar: Int,
    val beloep: Int,
    val maanedligBeloep: Int
)

data class SimulertPensjonBeholdningPeriode(
    val pensjonBeholdning: Double,
    val garantipensjonBeholdning: Double,
    val garantitilleggBeholdning: Double,
    val datoFom: LocalDate,
    var garantipensjonNivaa: SimulertGarantipensjonNivaa
)

data class SimulertGarantipensjonNivaa(
    val beloep: Double?,
    val satsType: String?,
    val sats: Double?,
    val anvendtTrygdetid: Int?
)

data class SimulertDelytelse(
    val type: YtelseskomponentTypeEnum,
    val beloep: Int
)
