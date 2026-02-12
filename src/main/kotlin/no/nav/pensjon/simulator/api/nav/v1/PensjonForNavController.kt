package no.nav.pensjon.simulator.api.nav.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.api.nav.v1.acl.spec.SimuleringSpecDto
import no.nav.pensjon.simulator.api.nav.v1.acl.spec.SimuleringSpecMapperForNav
import no.nav.pensjon.simulator.api.nav.v1.acl.result.ProblemDto
import no.nav.pensjon.simulator.api.nav.v1.acl.result.ProblemTypeDto
import no.nav.pensjon.simulator.api.nav.v1.acl.result.SimuleringResultDto
import no.nav.pensjon.simulator.api.nav.v1.acl.result.SimuleringResultMapper
import no.nav.pensjon.simulator.api.nav.v1.acl.result.VilkaarsproevingsresultatDto
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.json.writeValueAsRedactedString
import no.nav.pensjon.simulator.tech.trace.TraceAid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper

/**
 * REST-controller for simulering av forskjellige pensjonstyper.
 * Tjenesten er ment å brukes internt i Nav.
 */
@RestController
@RequestMapping("api/nav/v1")
@SecurityRequirement(name = "BearerAuthentication")
class PensjonForNavController(
    private val service: SimuleringFacade,
    private val specMapper: SimuleringSpecMapperForNav,
    private val traceAid: TraceAid,
    private val jsonMapper: JsonMapper,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    @PostMapping("simuler-pensjon")
    @Operation(
        summary = "Simuler forskjellige typer av pensjon",
        description = "Lager en prognose for utbetaling av pensjon."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av pensjon utført, eller personen har utilstrekkelig opptjening/trygdetid"
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
    fun simulerPensjon(@RequestBody specDto: SimuleringSpecDto): ResponseEntity<SimuleringResultDto> {
        traceAid.begin()
        log.debug { "spec ${jsonMapper.writeValueAsRedactedString(specDto)}" }
        countCall(functionName = FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = specMapper.fromDto(specDto)
            registrerHendelse(simuleringstype = spec.type)

            val result: SimulertPensjonEllerAlternativ =
                service.simulerAlderspensjon(spec, inkluderPensjonHvisUbetinget = false)

            val resultDto: SimuleringResultDto = SimuleringResultMapper.toDto(result)
            ResponseEntity.status(resultDto.problem?.kode?.httpStatus ?: HttpStatus.OK).body(resultDto)
        } catch (e: Exception) {
            log.error(e) { "$FUNCTION_ID intern feil for spec ${jsonMapper.writeValueAsRedactedString(specDto)}" }
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
        private const val TJENESTE = "intern simulering av pensjon"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID = "int-pen" // Nav-intern simulering av pensjon

        private fun problem(e: Exception, type: ProblemTypeDto) =
            SimuleringResultDto(
                alderspensjonListe = emptyList(),
                alderspensjonMaanedsbeloep = null,
                tidsbegrensetOffentligAfp = null,
                privatAfpListe = emptyList(),
                livsvarigOffentligAfpListe = emptyList(),
                vilkaarsproevingsresultat = VilkaarsproevingsresultatDto(erInnvilget = false, alternativ = null),
                primaerTrygdetid = null,
                pensjonsgivendeInntektListe = emptyList(),
                problem = ProblemDto(kode = type, beskrivelse = extractMessageRecursively(e))
            )
    }
}
