package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import jakarta.validation.constraints.NotNull
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate
import java.util.*

/**
 * Data transfer object (DTO) som representerer inn-data (spesifikasjon) for
 * simulering av alderspensjon "versjon 3" (som brukes av Navs pensjonskalkulator).
 */
data class SimuleringSpecDto(
    val pid: String,
    val sivilstand: SivilstatusSpecDto,
    val uttaksar: Int? = null,
    val sisteInntekt: Int,
    val simuleringstype: SimuleringstypeSpecDto,
    val gradertUttak: GradertUttakSpecDto? = null,
    val heltUttak: HeltUttakSpecDto,
    val aarUtenlandsEtter16Aar: Int? = null,
    val fremtidigInntektListe: List<InntektSpecDto>? = null,
    val utenlandsperiodeListe: List<UtlandSpecDto>? = null,
    val eps: EpsSpecDto? = null,
    val offentligAfp: OffentligAfpSpecDto? = null
)

data class GradertUttakSpecDto(
    val grad: UttaksgradSpecDto? = null,
    val uttakFomAlder: AlderSpecDto? = null,
    val aarligInntekt: Int? = null
)

data class HeltUttakSpecDto(
    val uttakFomAlder: AlderSpecDto,
    val aarligInntekt: Int,
    val inntektTomAlder: AlderSpecDto
)

data class InntektSpecDto(
    val aarligInntekt: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fom: Date? = null
)

data class UtlandSpecDto(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fom: Date,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val tom: Date?,
    val land: String,
    val arbeidetUtenlands: Boolean
)

/**
 * Informasjon om ektefelle/partner/samboer (EPS).
 */
data class EpsSpecDto(
    val levende: LevendeEpsDto? = null,
    val avdoed: AvdoedEpsDto? = null
)

data class LevendeEpsDto(
    @field:NotNull val harInntektOver2G: Boolean, // 2G = 2 ganger grunnbeløpet
    @field:NotNull val harPensjon: Boolean
)

/**
 * Informasjon om avdød ektefelle/partner/samboer (EPS) er relevant for pensjon med gjenlevenderett.
 */
data class AvdoedEpsDto(
    @field:NotNull val pid: String,
    @field:NotNull val doedsdato: LocalDate,
    val medlemAvFolketrygden: Boolean? = null,
    val inntektFoerDoedBeloep: Int? = null,
    val inntektErOverGrunnbeloepet: Boolean? = null,
    val antallAarUtenlands: Int? = null
)

data class OffentligAfpSpecDto(
    val harInntektMaanedenFoerUttak: Boolean? = null,
    val afpOrdning: AfpOrdningTypeSpecDto? = null,
    val innvilgetLivsvarigAfp: InnvilgetLivsvarigOffentligAfpSpecDto? = null
)

/**
 * Spesifiserer egenskapene til en løpende livsvarig AFP i offentlig sektor.
 */
data class InnvilgetLivsvarigOffentligAfpSpecDto(
    @field:NotNull val aarligBruttoBeloep: Double,
    @field:NotNull @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val uttakFom: LocalDate,
    val sistRegulertGrunnbeloep: Int? = null
)

data class AlderSpecDto(
    val aar: Int,
    val maaneder: Int
)

enum class AfpOrdningTypeSpecDto(val internalValue: AFPtypeEnum) {
    KOMMUNAL(internalValue = AFPtypeEnum.AFPKOM),
    STATLIG(internalValue = AFPtypeEnum.AFPSTAT),
    FINANSNAERINGEN(internalValue = AFPtypeEnum.FINANS),
    KONVERTERT_PRIVAT(internalValue = AFPtypeEnum.KONV_K),
    KONVERTERT_OFFENTLIG(internalValue = AFPtypeEnum.KONV_O),
    LO_NHO_ORDNINGEN(internalValue = AFPtypeEnum.LONHO),
    SPEKTER(internalValue = AFPtypeEnum.NAVO),
}
