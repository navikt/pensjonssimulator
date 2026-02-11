package no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result

import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus

/**
 * The data transfer object representing a result of the 'simuler alderspensjon & privat AFP' service.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonResponseDto
 *   + AFP part of no.nav.tjeneste.ekstern.simulerepensjon.v1.meldinger.HentSimulertPensjonResponse
 */
data class SimuleringResultDto(
    val suksess: Boolean,
    val alderspensjonsperioder: List<ApOgPrivatAfpAlderspensjonsperiodeResultDto>,
    val privatAfpPerioder: List<ApOgPrivatAfpPrivatAfpPeriodeResultDto>, // from HentSimulertPensjonResponse
    val harNaavaerendeUttak: Boolean,
    val harTidligereUttak: Boolean,
    val harLoependePrivatAfp: Boolean,
    val problem: ProblemDto? = null
)

data class ProblemDto(
    val kode: ProblemTypeDto,
    val beskrivelse: String
)

enum class ProblemTypeDto(
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
    SERVERFEIL(internalValue = ProblemType.SERVERFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR)
}

/**
 * The DTO representing 'alderspensjonsperiode' in result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.Pensjonsperiode
 *    + no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.SimulertFleksibelAlderspensjonPeriode
 *    + no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.UttaksgradPeriode
 */
data class ApOgPrivatAfpAlderspensjonsperiodeResultDto(
    val alder: Int, // from SimulertFleksibelAlderspensjonPeriode
    val beloep: Int, // from Pensjonsperiode, corresponds to 'beloep' in SimulertFleksibelAlderspensjonPeriode
    val datoFom: String, // from Pensjonsperiode
    // NB: In Dto for TPO this is not a list:
    val uttaksperiode: List<ApOgPrivatAfpUttaksperiodeResultDto> // from SimulertFleksibelAlderspensjonPeriode
)

/**
 * The DTO representing 'privat AFP-periode' in result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.SimulertAfpPrivatPeriode
 */
data class ApOgPrivatAfpPrivatAfpPeriodeResultDto(
    val alder: Int, // antall år
    val beloep: Int
)

/**
 * The DTO representing 'uttaksperiode' in result of 'simuler alderspensjon & privat AFP'.
 * ----------
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.UttaksgradPeriode
 */
data class ApOgPrivatAfpUttaksperiodeResultDto(
    val startmaaned: Int,
    val uttaksgrad: Int
)
