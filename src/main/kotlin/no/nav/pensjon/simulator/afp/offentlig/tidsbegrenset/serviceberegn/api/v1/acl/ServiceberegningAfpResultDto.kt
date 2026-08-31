package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus
import java.time.LocalDate

@JsonInclude(NON_NULL)
data class ServiceberegningAfpResultDto(
    @field:Schema(description = "beregnet avtalefestet pensjon (AFP)")
    val beregnetAfp: ServiceberegningFolketrygdberegnetAfpDto?,

    @field:Schema(description = "liste over årlig opptjening")
    @field:NotNull
    val opptjeningListe: List<ServiceberegningOpptjeningDto>,

    @field:Schema(description = "eventuelt problem oppstått i simuleringsprosessen")
    val problem: ServiceberegningProblemDto? = null
)

@JsonInclude(NON_NULL)
data class ServiceberegningFolketrygdberegnetAfpDto(
    @field:Schema(description = "totalt AFP-beløp")
    @field:NotNull
    val afpTotalbeloep: Int,

    @field:Schema(description = "virkningsdato (fra og med)")
    val virkningFom: LocalDate?,

    @field:Schema(description = "tidligere arbeidsinntekt (kronebeløp)")
    val tidligereArbeidsinntekt: Int?,

    @field:Schema(description = "folketrygdens grunnbeløp")
    val grunnbeloep: Int?,

    @field:Schema(description = "sluttpoengtall")
    val sluttpoengtall: Double?,

    @field:Schema(description = "trygdetid (antall år)")
    val trygdetid: Int?,

    @field:Schema(description = "antall poengår")
    val poengaar: Int?,

    @field:Schema(description = "antall poengår før 1992")
    val poengaarFoer1992: Int?,

    @field:Schema(description = "antall poengår etter 1991")
    val poengaarEtter1991: Int?,

    @field:Schema(description = "grunnpensjon (kronebeløp)")
    val grunnpensjon: Int?,

    @field:Schema(description = "tilleggspensjon (kronebeløp)")
    val tilleggspensjon: Int?,

    @field:Schema(description = "AFP-tillegg (kronebeløp)")
    val afpTillegg: Int?,

    @field:Schema(description = "framtidige pensjonspoeng")
    val fpp: Double?,

    @field:Schema(description = "særtillegg (kronebeløp)")
    val saertillegg: Int?,

    @field:Schema(description = "AFP-grad (prosent)")
    val grad: Int?,

    @field:Schema(description = "hvorvidt AFP-en er avkortet")
    val erAvkortet: Boolean?
)

data class ServiceberegningOpptjeningDto(
    @field:Schema(description = "året opptjeningen skjedde")
    @field:NotNull
    val aarstall: Int,

    @field:Schema(description = "pensjonsgivende inntekt (kronebeløp)")
    @field:NotNull
    val pensjonsgivendeInntekt: Int,

    @field:Schema(description = "antall pensjonspoeng")
    @field:NotNull
    val pensjonspoeng: Double
)

data class ServiceberegningProblemDto(
    @field:Schema(description = "type problem")
    @field:NotNull
    val type: ServiceberegningProblemtypeDto,

    @field:Schema(description = "beskrivelse av problemet")
    @field:NotNull
    val beskrivelse: String
)

enum class ServiceberegningProblemtypeDto(
    val internalValue: ProblemType,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) {
    UGYLDIG_UTTAKSDATO(internalValue = ProblemType.UGYLDIG_UTTAKSDATO),
    UGYLDIG_UTTAKSGRAD(internalValue = ProblemType.UGYLDIG_UTTAKSGRAD),
    UGYLDIG_SIVILSTATUS(internalValue = ProblemType.UGYLDIG_SIVILSTATUS),
    UGYLDIG_INNTEKT(internalValue = ProblemType.UGYLDIG_INNTEKT),
    UGYLDIG_ANTALL_AAR(internalValue = ProblemType.UGYLDIG_ANTALL_AAR),
    UGYLDIG_PERSONIDENT(internalValue = ProblemType.UGYLDIG_PERSONIDENT),
    PERSON_IKKE_FUNNET(internalValue = ProblemType.PERSON_IKKE_FUNNET, httpStatus = HttpStatus.NOT_FOUND),
    PERSON_FOR_HOEY_ALDER(internalValue = ProblemType.PERSON_FOR_HOEY_ALDER),
    UTILSTREKKELIG_OPPTJENING(internalValue = ProblemType.UTILSTREKKELIG_OPPTJENING, httpStatus = HttpStatus.OK),
    UTILSTREKKELIG_TRYGDETID(internalValue = ProblemType.UTILSTREKKELIG_TRYGDETID, httpStatus = HttpStatus.OK),
    ANNEN_KLIENTFEIL(internalValue = ProblemType.ANNEN_KLIENTFEIL),
    IMPLEMENTASJONSFEIL(internalValue = ProblemType.IMPLEMENTASJONSFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    INTERN_DATAFEIL(internalValue = ProblemType.INTERN_DATA_INKONSISTENS, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    TREDJEPARTSFEIL(internalValue = ProblemType.TREDJEPARTSFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    ANNEN_SERVERFEIL(internalValue = ProblemType.ANNEN_SERVERFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR)
}