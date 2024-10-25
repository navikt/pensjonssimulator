package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.AlderspensjonService
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonResultMapperV4.resultV4
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonResultV4
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonSpecMapperV4.fromSpecV4
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonSpecV4
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST-controller for simulering av alderspensjon.
 * Tjenestene er ment å brukes av tjenestepensjonsordninger (TPO).
 * TPO gjør kall til pensjonssimulator "direkte" (via API-gateway).
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class TpoAlderspensjonController(
    private val service: AlderspensjonService,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v4/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon",
        description = "Lager en prognose for utbetaling av alderspensjon.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (2) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjon(
        @RequestBody specV4: AlderspensjonSpecV4,
        request: HttpServletRequest
    ): AlderspensjonResultV4 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV4" }
        countCall(FUNCTION_ID)

        return try {
            val spec = fromSpecV4(specV4)
            request.setAttribute("pid", spec.pid)
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid)

            resultV4(timed(service::simulerAlderspensjon, spec, FUNCTION_ID))
                .also { log.debug { "$FUNCTION_ID response: $it" } }
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } catch (e: InvalidEnumValueException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon"
        private const val FUNCTION_ID = "ap"
    }
}
