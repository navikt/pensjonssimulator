package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import jakarta.validation.constraints.NotNull
import no.nav.pensjon.simulator.api.nav.v1.acl.UttaksgradDto
import java.time.LocalDate

/**
 * Data transfer object (DTO) som representerer inn-data (spesifikasjon) for
 * simulering av alderspensjon "versjon 3" (som brukes av Navs pensjonskalkulator).
 */
data class SimuleringSpecDto(
    @field:NotNull val pid: String,
    @field:NotNull val sivilstatus: SivilstatusSpecDto,
    @field:NotNull val sisteInntekt: Int,
    @field:NotNull val simuleringstype: SimuleringstypeSpecDto,
    val gradertUttak: GradertUttakSpecDto? = null,
    @field:NotNull val heltUttak: HeltUttakSpecDto,
    val aarUtenlandsEtter16Aar: Int? = null,
    val fremtidigInntektListe: List<InntektSpecDto>? = null,
    val utenlandsperiodeListe: List<UtlandSpecDto>? = null,
    val eps: EpsSpecDto? = null,
    val offentligAfp: OffentligAfpSpecDto? = null
)

data class GradertUttakSpecDto(
    val grad: UttaksgradDto? = null,
    val uttakFomAlder: AlderSpecDto? = null,
    val aarligInntekt: Int? = null
)

data class HeltUttakSpecDto(
    @field:NotNull val uttakFomAlder: AlderSpecDto,
    @field:NotNull val aarligInntekt: Int,
    @field:NotNull val inntektTomAlder: AlderSpecDto
)

data class InntektSpecDto(
    val aarligInntekt: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fom: LocalDate? = null
)

data class UtlandSpecDto(
    @field:NotNull @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fom: LocalDate,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val tom: LocalDate?,
    @field:NotNull val land: String, // must correspond to LandkodeEnum values
    @field:NotNull val arbeidetUtenlands: Boolean
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
 * Ref. Folketrygdloven kap. 17 - lovdata.no/lov/1997-02-28-19/KAPITTEL_5-4
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
    @field:NotNull val aar: Int,
    @field:NotNull val maaneder: Int
)
