package no.nav.pensjon.simulator.api.nav.v1.acl.result

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus

@JsonInclude(NON_NULL)
data class SimuleringResultDto(
    @field:NotNull val alderspensjonListe: List<AlderspensjonDto>,
    val alderspensjonMaanedsbeloep: UttaksbeloepDto?,
    @field:NotNull val livsvarigOffentligAfpListe: List<AldersbestemtUtbetalingDto>,
    val tidsbegrensetOffentligAfp: TidsbegrensetOffentligAfpDto?,
    @field:NotNull val privatAfpListe: List<PrivatAfpDto>,
    val primaerTrygdetid: TrygdetidDto?,
    @field:NotNull val vilkaarsproevingsresultat: VilkaarsproevingsresultatDto,
    @field:NotNull val pensjonsgivendeInntektListe: List<AarligBeloepDto>,
    val problem: ProblemDto? = null
)

@JsonInclude(NON_NULL)
data class AlderspensjonDto(
    @field:NotNull val alderAar: Int,
    @field:NotNull val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonsbeholdningFoerUttak: Int?,
    val sluttpoengtall: Double?,
    val poengaarFoer92: Int?,
    val poengaarEtter91: Int?,
    val forholdstall: Double?,
    val grunnpensjon: Int?,
    val tilleggspensjon: Int?,
    val pensjonstillegg: Int?,
    val skjermingstillegg: Int?,
    val kapittel19Pensjon: Kapittel19PensjonDto?,
    val kapittel20Pensjon: Kapittel20PensjonDto?
)

@JsonInclude(NON_NULL)
data class Kapittel19PensjonDto(
    val andelsbroek: Double?,
    val trygdetidAntallAar: Int?,
    val gjenlevendetillegg: Int?
)

@JsonInclude(NON_NULL)
data class Kapittel20PensjonDto(
    val andelsbroek: Double?,
    val trygdetidAntallAar: Int?
)

@JsonInclude(NON_NULL)
data class UttaksbeloepDto(
    val gradertUttakBeloep: Int?,
    @field:NotNull val heltUttakBeloep: Int
)

data class AldersbestemtUtbetalingDto(
    @field:NotNull val alderAar: Int,
    @field:NotNull val beloep: Int,
    @field:NotNull val maanedligBeloep: Int
)

data class TidsbegrensetOffentligAfpDto(
    @field:NotNull val alderAar: Int,
    @field:NotNull val totaltAfpBeloep: Int,
    @field:NotNull val tidligereArbeidsinntekt: Int,
    @field:NotNull val grunnbeloep: Int,
    @field:NotNull val sluttpoengtall: Double,
    @field:NotNull val trygdetid: Int,
    @field:NotNull val poengaarTom1991: Int,
    @field:NotNull val poengaarFom1992: Int,
    @field:NotNull val grunnpensjon: Int,
    @field:NotNull val tilleggspensjon: Int,
    @field:NotNull val afpTillegg: Int,
    @field:NotNull val saertillegg: Int,
    @field:NotNull val afpGrad: Int,
    @field:NotNull val erAvkortet: Boolean
)

data class PrivatAfpDto(
    @field:NotNull val alderAar: Int,
    @field:NotNull val beloep: Int,
    @field:NotNull val kompensasjonstillegg: Int,
    @field:NotNull val kronetillegg: Int,
    @field:NotNull val livsvarig: Int,
    @field:NotNull val maanedligBeloep: Int
)

@JsonInclude(NON_NULL)
data class VilkaarsproevingsresultatDto(
    @field:NotNull val erInnvilget: Boolean,
    val alternativ: UttaksparametreDto?
)

data class AarligBeloepDto(
    @field:NotNull val aarstall: Int,
    @field:NotNull val beloep: Int
)

/**
 * For 'erUtilstrekkelig' gjelder:
 * - Kapittel 19: Angir om trygdetiden er for kort for alderspensjon
 * - Kapittel 20: Angir om trygdetiden er for kort for garantipensjon
 */
data class TrygdetidDto(
    @field:NotNull val antallAar: Int,
    @field:NotNull val erUtilstrekkelig: Boolean
)

@JsonInclude(NON_NULL)
data class UttaksparametreDto(
    val gradertUttakAlder: AlderDto?,
    @field:NotNull val uttaksgrad: Int,
    @field:NotNull val heltUttakAlder: AlderDto
)

data class AlderDto(
    @field:NotNull val aar: Int,
    @field:NotNull val maaneder: Int
)

data class ProblemDto(
    @field:NotNull val kode: ProblemTypeDto,
    @field:NotNull val beskrivelse: String
)

enum class ProblemTypeDto(val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST) {
    UGYLDIG_UTTAKSDATO,
    UGYLDIG_UTTAKSGRAD,
    UGYLDIG_SIVILSTATUS,
    UGYLDIG_INNTEKT,
    UGYLDIG_ANTALL_AAR,
    UGYLDIG_PERSONIDENT,
    PERSON_IKKE_FUNNET(httpStatus = HttpStatus.NOT_FOUND),
    PERSON_FOR_HOEY_ALDER,
    UTILSTREKKELIG_OPPTJENING(httpStatus = HttpStatus.OK),
    UTILSTREKKELIG_TRYGDETID(httpStatus = HttpStatus.OK),
    ANNEN_KLIENTFEIL,
    SERVERFEIL(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR)
}