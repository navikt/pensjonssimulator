package no.nav.pensjon.simulator.api.samhandler.np.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result.SimuleringResultMapper
import no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result.SimuleringResultDto
import no.nav.pensjon.simulator.api.samhandler.np.v2.acl.spec.SimuleringSpecMapperForNorskPensjon
import no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result.ProblemTypeDto
import no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result.ProblemDto
import no.nav.pensjon.simulator.api.samhandler.np.v2.acl.spec.SimuleringSpecDto
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.orch.AlderspensjonOgPrivatAfpService
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST-controller for simulering av alderspensjon og privat AFP.
 * Tjenesten er ment å brukes av Norsk Pensjon (som gjør kall til pensjonssimulator via API-gateway).
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class PensjonForNorskPensjonController(
    private val service: AlderspensjonOgPrivatAfpService,
    private val specMapper: SimuleringSpecMapperForNorskPensjon,
    private val traceAid: TraceAid,
    statistikk: StatistikkService,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    //TODO Avtal med Norsk Pensjon å endre URL til v2/simuler-alderspensjon-privat-afp
    @PostMapping("v3/simuler-alderspensjon-privat-afp")
    @Operation(
        summary = "Simuler alderspensjon og privat AFP for samhandler (Dto)",
        description = "Lager en prognose for utbetaling av alderspensjon og privat avtalefestet pensjon" +
                " (versjon 3 av tjenesten)." +
                "\\\n\\\n*Scope*: **nav:pensjon/simulering/alderspensjonogprivatafp**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon og privat AFP utført, eller personen har utilstrekkelig opptjening/trygdetid"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Simulering kunne ikke utføres pga. manglende/feilaktig autentisering."
            ),
            ApiResponse(
                responseCode = "403",
                description = "Simulering kunne ikke utføres pga. manglende tilgang til tjenesten."
            ),
            ApiResponse(
                responseCode = "404",
                description = "Simulering kunne ikke utføres fordi angitt person ikke finnes i systemet."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Simulering kunne ikke utføres pga. feil i systemet."
            )
        ]
    )
    fun simulerAlderspensjonOgPrivatAfpDto(
        @RequestBody specDto: SimuleringSpecDto,
        request: HttpServletRequest
    ): ResponseEntity<SimuleringResultDto> {
        traceAid.begin()
        countCall(functionName = FUNCTION_ID_Dto)

        return try {
            val spec: SimuleringSpec = specMapper.fromDto(specDto)
            registrerHendelse(simuleringstype = spec.type)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid!!)
            val result = SimuleringResultMapper.toDto(service.simuler(spec))
            ResponseEntity.status(result.problem?.kode?.httpStatus ?: HttpStatus.OK).body(result)
        } catch (e: Exception) {
            log.error(e) { "$FUNCTION_ID_Dto intern feil for spec $specDto" }
            throw e
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    @ExceptionHandler(value = [Exception::class])
    private fun internalError(e: Exception): ResponseEntity<SimuleringResultDto> =
        with(ProblemTypeDto.SERVERFEIL) {
            ResponseEntity.status(this.httpStatus).body(problem(e, type = this))
        }

    private companion object {
        private const val TJENESTE = "simulering av alderspensjon/privat AFP"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID_Dto = "sam-ap-pafp"

        private fun problem(e: Exception, type: ProblemTypeDto) =
            SimuleringResultDto(
                suksess = false,
                alderspensjonsperioder = emptyList(),
                privatAfpPerioder = emptyList(),
                harNaavaerendeUttak = false,
                harTidligereUttak = false,
                harLoependePrivatAfp = false,
                problem = ProblemDto(kode = type, beskrivelse = e.message ?: "Ukjent feil - ${e.javaClass.simpleName}")
            )
    }
}