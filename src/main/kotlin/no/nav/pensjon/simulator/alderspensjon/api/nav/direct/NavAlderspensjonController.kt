package no.nav.pensjon.simulator.alderspensjon.api.nav.direct

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec.NavSimuleringSpecMapperV3.fromNavSimuleringSpecV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec.NavSimuleringSpecV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result.NavSimuleringResultMapperV3.mapNavSimuleringResultV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result.NavSimuleringResultV3
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class NavAlderspensjonController(
    private val service: SimuleringFacade,
    private val generelleDataHolder: GenerelleDataHolder,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    /**
     * This shall replace PEN service /springapi/simulering/alderspensjon
     */
    @PostMapping("v3/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon V3 for Nav-klient",
        description = "Lager en prognose for utbetaling av alderspensjon, basert på Nav-lagret info og input fra bruker.",
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
        @RequestBody specV3: NavSimuleringSpecV3,
        request: HttpServletRequest
    ): NavSimuleringResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV3" }
        countCall(FUNCTION_ID)

        return try {
            val foedselDato: LocalDate = generelleDataHolder.getPerson(Pid(specV3.pid)).foedselDato
            val spec: SimuleringSpec = fromNavSimuleringSpecV3(specV3, foedselDato)

            val result: SimulertPensjonEllerAlternativ =
                service.simulerAlderspensjon(spec, foedselDato, inkluderPensjonHvisUbetinget = false)

            mapNavSimuleringResultV3(result)
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
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon for Nav-klient"
        private const val FUNCTION_ID = "nav-ap"
    }
}
