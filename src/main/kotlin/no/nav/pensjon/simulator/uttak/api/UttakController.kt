package no.nav.pensjon.simulator.uttak.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.json.writeValueAsRedactedString
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import no.nav.pensjon.simulator.uttak.UttakService
import no.nav.pensjon.simulator.uttak.api.acl.*
import no.nav.pensjon.simulator.uttak.api.acl.UttakResultMapperV1.resultV1
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.json.JsonMapper

@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class UttakController(
    private val service: UttakService,
    private val specMapper: UttakSpecMapperV1,
    private val traceAid: TraceAid,
    private val jsonMapper: JsonMapper,
    statistikk: StatistikkService,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/tidligst-mulig-uttak")
    @Operation(
        summary = "Tidligst mulig uttak",
        description = "Finner den tidligst mulige dato for uttak av alderspensjon." +
                "\\\n\\\n*Scope*:" +
                "\\\n– Uten delegering: **nav:pensjonssimulator:simulering**" +
                "\\\n– Med delegering: **nav:pensjon/simulering.read**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Utledning av uttaksdato utført"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Utledning kunne ikke utføres pga. uakseptable inndata."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Utledning kunne ikke utføres pga. manglende/feilaktig autentisering."
            ),
            ApiResponse(
                responseCode = "403",
                description = "Utledning kunne ikke utføres pga. manglende tilgang til tjenesten."
            ),
            ApiResponse(
                responseCode = "404",
                description = "Utledning kunne ikke utføres fordi angitt person ikke finnes i systemet."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Utledning kunne ikke utføres pga. feil i systemet."
            )
        ]
    )
    fun tidligstMuligUttak(
        @RequestBody specV1: TidligstMuligUttakSpecV1,
        request: HttpServletRequest
    ): ResponseEntity<TidligstMuligUttakResultV1> {
        traceAid.begin(request)
        log.debug { "spec ${jsonMapper.writeValueAsRedactedString(specV1)}" }
        countCall(FUNCTION_ID)

        return try {
            val pid = Pid(specV1.personId)

            if (pid.isValid)
                request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, pid)
            else
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ugyldig personId: '${specV1.personId}'")

            verifiserAtBrukerTilknyttetTpLeverandoer(pid)
            val spec: SimuleringSpec = specMapper.fromSpecV1(specV1)
            registrerHendelse(simuleringstype = spec.type)

            val result = resultV1(
                source = timed(
                    function = service::finnTidligstMuligUttak,
                    argument = spec,
                    functionName = FUNCTION_ID
                )
            )

            ResponseEntity.status(result.feil?.type?.httpStatus ?: HttpStatus.OK).body(result)
        } catch (e: Exception) {
            log.error(e) { "$FUNCTION_ID intern feil for spec ${jsonMapper.writeValueAsRedactedString(specV1)}" }
            throw e
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    @ExceptionHandler(value = [Exception::class])
    private fun internalError(e: Exception): ResponseEntity<TidligstMuligUttakResultV1> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem(e))

    private companion object {
        private const val TJENESTE = "simulering for TMU"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID = "tmu"

        private fun problem(e: Exception) =
            TidligstMuligUttakResultV1(
                tidligstMuligeUttakstidspunktListe = emptyList(),
                feil = TidligstMuligUttakFeilV1(
                    type = TidligstMuligUttakFeilTypeV1.TEKNISK_FEIL,
                    beskrivelse = e.message ?: e.javaClass.simpleName
                )
            )
    }
}
